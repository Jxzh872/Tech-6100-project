import RPi.GPIO as GPIO
import time

class SG90Servo:
    def __init__(self, pin=16, frequency=50):
        """
        Initialize the SG90 servo motor using RPi.GPIO.
        :param pin: GPIO pin connected to the servo signal wire.
        :param frequency: PWM frequency (default is 50 Hz).
        """
        self.pin = pin
        self.frequency = frequency

        # Set up GPIO
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.pin, GPIO.OUT)

        # Initialize PWM
        self.pwm = GPIO.PWM(self.pin, self.frequency)
        self.pwm.start(0)  # Start with 0% duty cycle
        print(f"Servo initialized on GPIO {self.pin} with frequency {self.frequency} Hz.")

    def set_angle(self, angle):
        """
        Set the servo to a specific angle.
        :param angle: Desired angle in degrees (0 to 180).
        """
        if 0 <= angle <= 180:
            duty_cycle = 2.5 + (angle / 180) * 10  # Map angle to 2.5% to 12.5% duty cycle
            self.pwm.ChangeDutyCycle(duty_cycle)
            time.sleep(0.3)  # Allow the servo to reach the desired position
            self.pwm.ChangeDutyCycle(0)  # Stop sending the signal to reduce shaking
        else:
            print("Angle out of range. Must be between 0 and 180 degrees.")

    def grip_close(self):
        """Close the gripper (rotate to 0 degrees)."""
        print("Closing gripper...")
        self.set_angle(0)

    def grip_open(self):
        """Open the gripper (rotate to 45 degrees)."""
        print("Opening gripper...")
        self.set_angle(100)

    def cleanup(self):
        """Clean up resources."""
        print("Cleaning up...")
        self.pwm.stop()
        GPIO.cleanup()
        print("Program ended.")
        
if __name__ == "__main__":
    # Create an instance of SG90Servo
    gripper = SG90Servo(pin=16)
    try:
        while True:
        # Open and close the gripper
            gripper.grip_open()
            time.sleep(2)
            gripper.grip_close()
            time.sleep(2)

    except KeyboardInterrupt:
        print("Program interrupted.")
        
    finally:
        gripper.cleanup()
