from servo import SG90Servo
import time

from stepper_motor import motor

def rasie_heigh(height):
    #the function convert height to step
    #negative lift up, full range 1312 in 124mm 0.094mm/step
    step = 0 - round(height / 0.09451219512, 0)
    step = int(step)
    print (step)
    return step

Lifter = motor.step_motor

# Create an instance of SG90Servo
gripper = SG90Servo(pin=16)

try:
    # the lifter range is from 60mm to 184mm (range 124mm)
    Lifter(rasie_heigh(-124), 0.001)  
    Lifter(rasie_heigh(124), 0.001)
    
#     # Open and close the gripper
#     gripper.grip_open()
#     time.sleep(2)
#     gripper.grip_close()
#     time.sleep(2)

except KeyboardInterrupt:
    print("Program interrupted.")
    
finally:
    gripper.cleanup()
    motor.cleanup()