"""
Tool for testing UART communitation - ZX
"""

import serial
import time

# UART Configuration
PORT = '/dev/ttyAMA2'  # Use the correct UART port
BAUDRATE = 115200
TIMEOUT = 2

def main():
    try:
        print(f"Initializing UART: {PORT}, Baudrate: {BAUDRATE}")
        ser = serial.Serial(port=PORT, baudrate=BAUDRATE, timeout=TIMEOUT)
        time.sleep(1)  # Allow time for stabilization

        if not ser.is_open:
            print("Error: Cannot open serial port")
            return

        print("UART ready. Type a message to send (or type 'exit' to quit).")

        while True:
            # Get user input message
            message = input("Enter message: ").strip()
            if message.lower() == "exit":
                break  # Exit loop

            # Send the message with a newline (MicroPython expects \n)
            ser.write(f"{message}\n".encode('utf-8'))
            print(f"Sent: {message}")

            # Read response from Pico
            response = ser.readline().decode('utf-8').strip()
            if response:
                print(f"Received from Pico: {response}")
            else:
                print("No response received.")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        if 'ser' in locals() and ser.is_open:
            ser.close()
            print("Serial port closed.")

if __name__ == "__main__":
    main()

