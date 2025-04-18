"""
Thsi code is to use the MPU 6050 to measure the robot Yaw angle. - ZX
modify from
source: https://github.com/micropython-IMU/micropython-mpu9x50/tree/master
"""
from machine import I2C, Pin, Timer
from imu import MPU6050
import time, math

# Initialize I2C interface
i2c = I2C(0, scl=Pin(21), sda=Pin(20), freq=400000)
imu = MPU6050(i2c)
imu.filter_range = 6  # Set gyro filter range

# Measure and calculate the zero-offset of the Z-axis gyro
def measure_zero_offset(samples=500, delay=0.01):
    sum_z = 0
    for _ in range(samples):
        sum_z += imu.gyro.xyz[2]
        time.sleep(delay)
    return sum_z / samples

zero_point_offset = measure_zero_offset()
print("Gyro zero offset: {:.2f}°/s".format(zero_point_offset))

# Yaw angle accumulation
yaw = 0.0
last_update = time.ticks_ms()

# Timer callback to update yaw angle
def update_yaw(timer):
    global yaw, last_update
    current_time = time.ticks_ms()
    elapsed = time.ticks_diff(current_time, last_update) / 1000  # Convert ms to seconds
    last_update = current_time

    gyro_z = imu.gyro.xyz[2] - zero_point_offset
    if abs(gyro_z) < 0.5:  # Deadband to reduce drift
        gyro_z = 0
    yaw += gyro_z * elapsed

# Start periodic timer to update yaw every 10ms
timer = Timer()
timer.init(period=10, mode=Timer.PERIODIC, callback=update_yaw)

# Main loop to print yaw angle
while True:
    print("Yaw: {:.2f}°".format(yaw % 360))  # Normalize yaw to 0–359°
    time.sleep(0.1)