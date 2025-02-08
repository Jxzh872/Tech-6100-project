# stepper_motor.py
import RPi.GPIO as GPIO
import time

class StepperMotor:
    def __init__(self, in1=5, in2=6, in3=13, in4=19):
        """
        Initialize the stepper motor with default or custom GPIO pins.
        :param in1: GPIO pin connected to IN1 on the ULN2003 board.
        :param in2: GPIO pin connected to IN2 on the ULN2003 board.
        :param in3: GPIO pin connected to IN3 on the ULN2003 board.
        :param in4: GPIO pin connected to IN4 on the ULN2003 board.
        """
        self.IN1 = in1
        self.IN2 = in2
        self.IN3 = in3
        self.IN4 = in4
        
        # Set up GPIO pins
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.IN1, GPIO.OUT)
        GPIO.setup(self.IN2, GPIO.OUT)
        GPIO.setup(self.IN3, GPIO.OUT)
        GPIO.setup(self.IN4, GPIO.OUT)
        
        # Define half-step sequence
        self.half_step_sequence = [
            [1, 0, 0, 0],
            [1, 1, 0, 0],
            [0, 1, 0, 0],
            [0, 1, 1, 0],
            [0, 0, 1, 0],
            [0, 0, 1, 1],
            [0, 0, 0, 1],
            [1, 0, 0, 1],
        ]

    def set_step_pins(self, step):
        """Set the GPIO pins to the current step."""
        GPIO.output(self.IN1, step[0])
        GPIO.output(self.IN2, step[1])
        GPIO.output(self.IN3, step[2])
        GPIO.output(self.IN4, step[3])

    def step_motor(self, steps, delay):
        """
        Move the motor by a specified number of steps.
        :param steps: Number of steps to rotate (positive for clockwise, negative for counter-clockwise).
        :param delay: Time delay between each step (in seconds).
        """
        for _ in range(abs(steps)):
            sequence = self.half_step_sequence if steps > 0 else reversed(self.half_step_sequence)
            for step in sequence:
                self.set_step_pins(step)
                time.sleep(delay)

    def cleanup(self):
        """Clean up GPIO pins."""
        GPIO.cleanup()

# Automatically create a default instance of StepperMotor for easy use
motor = StepperMotor()
