import gc
import time
from motor import Motor, motor2040
from encoder import Encoder
from pimoroni import Button, PID, REVERSED_DIR

# Wheel friendly names
FL = 0
FR = 1
RL = 2
RR = 3

# Motor Configuration
GEAR_RATIO = 125                         # Gear ratio of the motors
COUNTS_PER_REV = 14 * GEAR_RATIO         # Encoder counts per revolution

SPEED_SCALE = 4                           # Scale factor for real-world speed
UPDATES = 500                             # Motor update frequency (Hz)
UPDATE_RATE = 1 / UPDATES
TIME_FOR_EACH_MOVE = 2                    # Duration per movement step
UPDATES_PER_MOVE = int(TIME_FOR_EACH_MOVE * UPDATES)
PRINT_DIVIDER = 4                         # Print every N updates

DRIVING_SPEED = 10                         # Speed range: 0 to SPEED_SCALE

# PID Configuration
VEL_KP = 30.0
VEL_KI = 0.0
VEL_KD = 0.4

# Free up resources before setting up encoders
gc.collect()

# Motor Setup
MOTOR_PINS = [motor2040.MOTOR_A, motor2040.MOTOR_B, motor2040.MOTOR_C, motor2040.MOTOR_D]
motors = [Motor(pin, speed_scale=SPEED_SCALE) for pin in MOTOR_PINS]

# Encoder Setup
ENCODER_PINS = [motor2040.ENCODER_A, motor2040.ENCODER_B, motor2040.ENCODER_C, motor2040.ENCODER_D]
ENCODER_NAMES = ["RR", "RL", "FL", "FR"]
encoders = [
    Encoder(0, i, ENCODER_PINS[i], counts_per_rev=COUNTS_PER_REV, count_microsteps=True)
    for i in range(motor2040.NUM_MOTORS)
]

# Reverse certain motors and encoders
motors[FL].direction(REVERSED_DIR)
motors[RL].direction(REVERSED_DIR)
encoders[FL].direction(REVERSED_DIR)
encoders[RL].direction(REVERSED_DIR)

# Button for stopping execution
user_sw = Button(motor2040.USER_SW)

# PID Controllers for speed control
vel_pids = [PID(VEL_KP, VEL_KI, VEL_KD, UPDATE_RATE) for _ in range(motor2040.NUM_MOTORS)]

# Motor Control Functions
def drive_forward(speed):
    for pid in vel_pids:
        pid.setpoint = speed

def turn_right(speed):
    vel_pids[FL].setpoint = speed
    vel_pids[FR].setpoint = -speed
    vel_pids[RL].setpoint = speed
    vel_pids[RR].setpoint = -speed

def strafe_right(speed):
    vel_pids[FL].setpoint = speed
    vel_pids[FR].setpoint = -speed
    vel_pids[RL].setpoint = -speed
    vel_pids[RR].setpoint = speed

def stop():
    for pid in vel_pids:
        pid.setpoint = 0

# Enable motors
for motor in motors:
    motor.enable()

update = 0
print_count = 0
sequence = 0
captures = [None] * motor2040.NUM_MOTORS

# Run loop
while not user_sw.raw():
    # Capture encoder values
    for i in range(motor2040.NUM_MOTORS):
        captures[i] = encoders[i].capture()

    # Apply PID control
    for i, motor in enumerate(motors):
        accel = vel_pids[i].calculate(captures[i].revolutions_per_second)
        new_speed = motor.speed() + (accel * UPDATE_RATE)
        motor.speed(new_speed)

    # Print status periodically
    if print_count == 0:
        status = ", ".join(f"{ENCODER_NAMES[i]}={captures[i].revolutions_per_second:.2f}" for i in range(motor2040.NUM_MOTORS))
        print(f"[{time.time():.2f}] {status}")

    print_count = (print_count + 1) % PRINT_DIVIDER
    update += 1

    # Movement sequence
    if update >= UPDATES_PER_MOVE:
        update = 0
        sequence = (sequence + 1) % 7

    if sequence == 0:
        drive_forward(DRIVING_SPEED)
    elif sequence == 1:
        drive_forward(-DRIVING_SPEED)
    elif sequence == 2:
        turn_right(DRIVING_SPEED)
    elif sequence == 3:
        turn_right(-DRIVING_SPEED)
    elif sequence == 4:
        strafe_right(DRIVING_SPEED)
    elif sequence == 5:
        strafe_right(-DRIVING_SPEED)
    elif sequence == 6:
        stop()

    time.sleep(UPDATE_RATE)

# Disable motors when exiting
for motor in motors:
    motor.disable()
print("Motors stopped.")