"""
Zongh X 2025-04-10

This program is strictly my own work. Any material
beyond course learning materials that is taken from
the Web or other sources is properly cited, giving
credit to the original author(s).

This code is the chassis motor control with pico and DVR8833, 
it receive command from the rpi via UART.
It able to move in onmidirection by use the mecanum wheel, 
"""

from machine import Pin, PWM
import math
import time

# Motor pin configuration (adjust direction based on actual wiring)
motorA_in1 = PWM(Pin(4))  # Front-right motor IN1 (forward)
motorA_in2 = PWM(Pin(5))  # Front-right motor IN2 (reverse)
motorB_in1 = PWM(Pin(8))  # Front-left motor IN1 (forward)
motorB_in2 = PWM(Pin(9))  # Front-left motor IN2 (reverse)
motorC_in1 = PWM(Pin(6))  # Rear-right motor IN1 (forward)
motorC_in2 = PWM(Pin(7))  # Rear-right motor IN2 (reverse)
motorD_in1 = PWM(Pin(10)) # Rear-left motor IN1 (forward)
motorD_in2 = PWM(Pin(11)) # Rear-left motor IN2 (reverse)

# Set PWM frequency (160Hz)
PWM_FREQ = 160
for motor in [motorA_in1, motorA_in2, motorB_in1, motorB_in2,
              motorC_in1, motorC_in2, motorD_in1, motorD_in2]:
    motor.freq(PWM_FREQ)

def move_motor(motor_in1, motor_in2, speed):
    """Controls a single motor with the given speed (range: -100 to 100)."""
    dead_zone = 25  # Ignore low-speed commands to eliminate jitter

    if abs(speed) < dead_zone:
        speed = 0

    duty = int(abs(speed) * 65535 / 100)  # Convert to 16-bit duty cycle

    if speed > 0:
        motor_in1.duty_u16(duty)
        motor_in2.duty_u16(1)
    elif speed < 0:
        motor_in1.duty_u16(1)
        motor_in2.duty_u16(duty)
    else:
        motor_in1.duty_u16(1)
        motor_in2.duty_u16(1)

def stop_all_motors():
    """Stops all motors (braking effect applied)."""
    for m in [(motorA_in1, motorA_in2), (motorB_in1, motorB_in2),
              (motorC_in1, motorC_in2), (motorD_in1, motorD_in2)]:
        m[0].duty_u16(65535)  # Apply braking
        m[1].duty_u16(65535)

    time.sleep_ms(100)  # Hold brake briefly

    for m in [(motorA_in1, motorA_in2), (motorB_in1, motorB_in2),
              (motorC_in1, motorC_in2), (motorD_in1, motorD_in2)]:
        move_motor(*m, 0)  # Fully stop

def mecanum_move(M_angle_deg, D_power, rotation_pwr):
    """
    Calculates wheel speeds for omni-directional movement using mecanum wheels.
    angle_deg: Movement direction in degrees (0–360)
    power:     Translational speed (%)
    rotation:  Rotational speed (%), positive is clockwise
    """
#   only rotation angle apply
    if M_angle_deg == 0 and D_power==0 and rotation_pwr < 0 or rotation_pwr > 0:
        speed_A = 0-rotation_pwr
        speed_B = rotation_pwr
        speed_C = 0-rotation_pwr
        speed_D = rotation_pwr
        scale = 1
    else:
        #calibrate the offset
        rotation_pwr = rotation_pwr + 0.06
        # Convert angle to radians and adjust reference
        angle_rad = math.radians((M_angle_deg + 90) % 360)
        x = math.cos(angle_rad) * D_power  # Left-right component
        y = math.sin(angle_rad) * D_power  # Forward-backward component

        # Mecanum drive formula including rotation
        speed_A = (y + x + rotation_pwr * (0.22+0.235)) # Front-right
        speed_B = (y - x - rotation_pwr * (0.22+0.235))  # Front-left
        speed_C = (y - x + rotation_pwr * (0.22+0.235))  # Rear-right
        speed_D = (y + x - rotation_pwr * (0.22+0.235))  # Rear-left
        
        # Normalize speeds to stay within [-100, 100]
        max_speed = max(abs(speed_A), abs(speed_B), abs(speed_C), abs(speed_D), 1)
        scale = min(100 / max_speed, 1)
    
    return (speed_A * scale, speed_B * scale,
            speed_C * scale, speed_D * scale)

def main():
    """
    UART command:
    - "STOP"               Immediately stop all motors
    - "<a>,<p>,<r>"        Combined movement and rotation
    """
    uart = machine.UART(0, baudrate=115200, tx=Pin(0), rx=Pin(1))

    while True:
        if uart.any():
            raw = uart.readline().decode().strip()

            # Emergency stop command
            if raw == "STOP":
                stop_all_motors()
                uart.write("ACK:EMERGENCY STOP\n")
                continue

            try:
                if raw.startswith('R'):  # Rotation-only mode
                    rotation = float(raw[1:])
                    angle, power = 0, 0
                else:
                    parts = list(map(float, raw.split(',')))
                    
                    if len(parts) == 3:    # Full movement + rotation
                        angle, power, rotation = parts
                    else:
                        raise ValueError("Invalid parameter count")

                a, b, c, d = mecanum_move(angle, power, rotation)

                # Drive motors
                move_motor(motorA_in1, motorA_in2, a)
                move_motor(motorB_in1, motorB_in2, b)
                move_motor(motorC_in1, motorC_in2, c)
                move_motor(motorD_in1, motorD_in2, d)

                # Send feedback for debugging
                uart.write(f"ACK: A{a:.0f} B{b:.0f} C{c:.0f} D{d:.0f}\n")

            except Exception as e:
                uart.write(f"ERROR: {str(e)}\n")
                stop_all_motors()

            time.sleep(0.1)  # Loop at 10Hz

if __name__ == "__main__":
    main()