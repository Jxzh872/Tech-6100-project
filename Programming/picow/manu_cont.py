from machine import Pin, PWM
import math
import time

# Motor pin configuration
motorA_in1 = PWM(Pin(4))  # Front-right
motorA_in2 = PWM(Pin(5))
motorB_in1 = PWM(Pin(8))  # Front-left
motorB_in2 = PWM(Pin(9))
motorC_in1 = PWM(Pin(6))  # Rear-right
motorC_in2 = PWM(Pin(7))
motorD_in1 = PWM(Pin(10)) # Rear-left
motorD_in2 = PWM(Pin(11))

# Set PWM frequency
PWM_FREQ = 16000
for motor in [motorA_in1, motorA_in2, motorB_in1, motorB_in2,
              motorC_in1, motorC_in2, motorD_in1, motorD_in2]:
    motor.freq(PWM_FREQ)

def move_motor(motor_in1, motor_in2, speed):
    speed = max(-100, min(100, speed))  # Limit speed range
    duty = int(abs(speed) * 65535 / 100)  # Convert to PWM duty cycle
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
    for m in [(motorA_in1, motorA_in2), (motorB_in1, motorB_in2),
              (motorC_in1, motorC_in2), (motorD_in1, motorD_in2)]:
        move_motor(*m, 0)

def mecanum_move(angle_deg, power):
    # Convert angle to radians
    angle_deg = (angle_deg + 90) % 360
    angle_rad = math.radians(angle_deg)
    
    # Calculate X/Y components
    x = math.cos(angle_rad) * power  # X-direction component (left/right)
    y = math.sin(angle_rad) * power  # Y-direction component (forward/backward)
    
    # Mecanum wheel movement formula
    speed_A = y + x  # Front-right
    speed_B = y - x  # Front-left
    speed_C = y - x  # Rear-right
    speed_D = y + x  # Rear-left
    
    # Normalize speed to [-100, 100]
    max_speed = max(abs(speed_A), abs(speed_B), abs(speed_C), abs(speed_D))
    if max_speed > 100:
        scale = 100 / max_speed
        speed_A *= scale
        speed_B *= scale
        speed_C *= scale
        speed_D *= scale
    
    return speed_A, speed_B, speed_C, speed_D

def main():
    uart = machine.UART(0, baudrate=115200, tx=Pin(0), rx=Pin(1))
    
    while True:
        if uart.any():
            command = uart.readline().decode().strip()
            
            if command == "STOP":
                stop_all_motors()
                uart.write("ACK: STOP\n")
                
            elif ',' in command:
                try:
                    angle, power = map(int, command.split(','))
                    a, b, c, d = mecanum_move(angle, power)
                    
                    move_motor(motorA_in1, motorA_in2, a)  # Front-right
                    move_motor(motorB_in1, motorB_in2, b)  # Front-left
                    move_motor(motorC_in1, motorC_in2, c)  # Rear-right
                    move_motor(motorD_in1, motorD_in2, d)  # Rear-left
                    
                    uart.write(f"ACK: {a:.1f},{b:.1f},{c:.1f},{d:.1f}\n")
                except:
                    uart.write("ERROR: Invalid command\n")
            time.sleep(0.1)

if __name__ == "__main__":
    main()
