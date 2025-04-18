"""
this QR code dectector was taken from https://core-electronics.com.au/guides/QR-codes-raspberry-pi/
it was slightly mofified to make it work for the needs
flask server : https://github.com/ramonus/flask-video-stream/blob/master/server.py
add distance measurement - Ryan, ZX
achieved the manual control for "swiper" - ZX

This program is strictly my own work. Any material
beyond course learning materials that is taken from
the Web or other sources is properly cited, giving
credit to the original author(s).
"""
import cv2
import numpy as np
import os
import serial
import socket
import time
import threading
import queue
from flask import Flask, Response
from servo import Gripper
from stepper_motor import motor

# Qt plugin fix
os.environ["QT_PLUGIN_PATH"] = "/usr/local/lib/python3.11/dist-packages/cv2/qt/plugins"

# Camera and QR setup
cap = cv2.VideoCapture(0, cv2.CAP_V4L2)
detector = cv2.QRCodeDetector()

# Communication setup UART and UDP
PORT = '/dev/ttyAMA2'
BAUDRATE = 115200
TIMEOUT = 0.2
UDP_IP = "0.0.0.0"
UDP_PORT = 5000

# Motor & gripper
Lifter = motor
gripper = Gripper(pin=16)

# Shared queue
command_queue = queue.Queue()


# QR code Detection
def process_qr_code(img):
    data, bbox, _ = detector.detectAndDecode(img)
    if bbox is None or len(bbox) == 0:
        return img

    bbox = bbox.astype(int)
    detected_width = np.linalg.norm(bbox[0][0] - bbox[0][1])

    # calibrated distance formula
    if detected_width > 0:
        distance_mm = 61399 * (detected_width ** -1.21)
    else:
        distance_mm = 0

    # Draw bounding box and info
    for i in range(len(bbox[0])):
        pt1 = tuple(bbox[0][i].ravel())
        pt2 = tuple(bbox[0][(i + 1) % len(bbox[0])].ravel())
        cv2.line(img, pt1, pt2, (255, 0, 0), 2)

    # Mark QR center and show data + distance
    x_center = int(bbox[0][:, 0].mean())
    y_center = int(bbox[0][:, 1].mean())
    cv2.circle(img, (x_center, y_center), 5, (0, 255, 0), -1)

    cv2.putText(img, f"Distance: {int(distance_mm)} mm", (x_center, y_center - 10),
                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 255), 2)
    cv2.putText(img, f"Detect: {data}", (x_center, y_center - 50),
                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (100, 255, 255), 2)
    return img


# Webcam Flask Stream 
def generate_frames():
    while True:
        ret, img = cap.read()
        if not ret:
            continue
        img = process_qr_code(img) # Annotate image with QR info
        _, buffer = cv2.imencode('.jpg', img)
        frame = buffer.tobytes()
        # Multipart stream
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

#Flask Web Server
app = Flask(__name__)

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/')
def index():
    return "<h1>QR Code Detector Stream</h1><img src='/video_feed' width='640'>"


# UDP Receiving Thread
def udp_listener():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind((UDP_IP, UDP_PORT))
    print("UDP listener running...")

    while True:
        data, _ = sock.recvfrom(1024)
        message = data.decode('utf-8').strip()
        print(f"Received: {message}")
        command_queue.put(message) # Add received message to the queue


# Command Processing
def control_handler():
    try:
        ser = serial.Serial(port=PORT, baudrate=BAUDRATE, timeout=TIMEOUT)
        time.sleep(1)
        if not ser.is_open:
            print("Error: Cannot open serial port")
            return

        while True:
            if command_queue.empty():
                time.sleep(0.01)
                continue

            message = command_queue.get()
            print(f"Processing command: {message}")

            # Check if message is joystick numeric command
            if message.replace(',', '').replace('-', '').isdigit():
                ser.write(f"{message}\n".encode('utf-8'))
                print(f"Sent to Pico: {message}")
            # Handle lift up/down
            elif message.lower() == "lift up":
                if not Lifter.is_busy() and not Lifter.is_upLSTri():
                    threading.Thread(target=Lifter.step_motor, args=(-16, 0.001)).start()
            elif message.lower() == "lift down":
                if not Lifter.is_busy() and not Lifter.is_lowLSTri():
                    threading.Thread(target=Lifter.step_motor, args=(16, 0.001)).start()
            # Gripper control
            elif message.lower() == "open gripper":
                gripper.gp_open(duration=0.5)
            elif message.lower() == "close gripper":
                gripper.gp_close(duration=0.6)
            # Read response from picow
            response = ser.readline().decode('utf-8', errors='ignore').strip()
            if response:
                print(f"Received from Pico: {response}")

    except Exception as e:
        print(f"Error: {e}")
    finally:
        gripper.cleanup()
        Lifter.cleanup()
        if 'ser' in locals() and ser.is_open:
            ser.close()


# MAIN
if __name__ == '__main__':
    
    # Start UDP
    udp_thread = threading.Thread(target=udp_listener, daemon=True)
    control_thread = threading.Thread(target=control_handler, daemon=True)
    
    udp_thread.start()
    control_thread.start()
    #Start Flask server
    app.run(host='0.0.0.0', port=8080, debug=False, threaded=True)

