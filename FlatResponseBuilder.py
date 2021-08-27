from os import write
from itertools import chain
import psycopg2
import csv
import datetime

header=[
    "VehicleID",
    "CreatedAt",
    "latitude",
    "longitude",
    "Altitude",
    "Dev TS",
    "VIN",
    "Gps Latitude",
    "Gps Longitude",
    "Gps altitude",
    "Gps Speed",
    "Gps Satellite Count",
    "Gps time",
    "Gps date",
    "Gps Acc",
    "Battery Voltage",
    "Orientation",
    "Fuel System Status",
    "Calculated Engine Load",
    "Engine Coolant Temperature",
    "Short Term Fuel Trim - Bank 1",
    "Long Term Fuel Trim - Bank 1",
    "Short Term Fuel Trim - Bank 2",
    "Long Term Fuel Trim - Bank 2",
    "Fuel Pressure",
    "Intake Manifold Absolute Pressure",
    "Engine Speed",
    "Vehicle Speed",
    "Timing Advance",
    "Intake Air Temperature",
    "Mass Airflow Sensor",
    "Throttle Position",
    "Commanded Secondary Air Status",
    "Oxygen Sensors in 2 Banks",
    "Oxygen Sensor 1",
    "Oxygen Sensor 2",
    "Oxygen Sensor 3",
    "Oxygen Sensor 4",
    "Oxygen Sensor 5",
    "Oxygen Sensor 6",
    "Oxygen Sensor 7",
    "Oxygen Sensor 8",
    "OBD Standard Confirmation",
    "Oxygen Sensors in 4 Banks",
    "AUX Input Status",
    "Runtime",
    "PIDs Supported (21-40)",
    "Distance Travelled with Malfunction",
    "Fuel Rail Pressure",
    "Fuel Rail Gauge Pressure",
    "Oxygen Sensor 1",
    "Oxygen Sensor 2",
    "Oxygen Sensor 3",
    "Oxygen Sensor 4",
    "Oxygen Sensor 5",
    "Oxygen Sensor 6",
    "Oxygen Sensor 7",
    "Oxygen Sensor 8",
    "Commanded EGR",
    "EGR Error",
    "Commanded Evaporative Purge",
    "Fuel Tank Level",
    "Warmups",
    "Distance Travelled Since Codes Cleared",
    "Evap. System Vapour Pressure",
    "Absolute Barometric Pressure",
    "Oxygen sensor 1",
    "Oxygen sensor 2",
    "Oxygen sensor 3",
    "Oxygen sensor 4",
    "Oxygen sensor 5",
    "Oxygen sensor 6",
    "Oxygen sensor 7",
    "Oxygen sensor 8",
    "Catalyst Temperature Bank 1 Sensor 1",
    "Catalyst Temperature Bank 2 Sensor 1",
    "Catalyst Temperature Bank 1 Sensor 2",
    "Catalyst Temperature Bank 2 Sensor 2",
    "PIDs Supported(41-60)",
    "Monitor Status This Drive Cycle",
    "Control Module Voltage",
    "Absolute Load Value",
    "Commanded Air Fuel Equivalence Ratio",
    "Relative Throttle Position",
    "Ambient Air Temperature",
    "Absolute Throttle Position B",
    "Absolute Throttle Position C",
    "Absolute Throttle Position D",
    "Absolute Throttle Position E",
    "Absolute Throttle Position F",
    "Commanded Throttle Actuator",
    "Time Run with MIL on",
    "Time Since Travel Codes Cleared",
    "Max Value for Fuel Air Eqv",
    "Max Value for Air Flow",
    "Fuel Tire",
    "Ethanol fuel Percent",
    "Abs. Evap. System Vap. Pressure",
    "Evap. System Vap. Pressure",
    "Short Term Secondary O2 Sensor A: Bank 1 B: bank 3",
    "Long Term Secondary O2 Sensor A: Bank 1 B: bank 3",
    "Short Term Secondary O2 Sensor A: Bank 2 B: bank 4",
    "Long Term Secondary O2 Sensor A: Bank 2 B: bank 4",
    "Fuel Rail Absolute Pressure",
    "Relative Accelerator Pedal Position",
    "Hybrid Battery Pack Remaining Life",
    "Engine Oil Temperature",
    "Fuel Injection Timing",
    "Engine Fuel Rate",
    "Emission Req. of Veh. Design",
    "PIDs Supported (61-80)",
    "Driver's Demand Engine - Percent Torque",
    "Actual Engine - Percent Torque",
    "Engine Reference Torque",
    "Engine Percent Torque Data",
    "Auxiliary Input/ Output supported",
    "Mass Air Flow Sensor",
    "Engine Coolant Temperature",
    "Intake Air Temperature Sensor",
    "Commanded EGR and EGR Error",
    "Commanded Diesel Intake air flow control and relative intake air flow position",
    "Exhaust Gas Recirculation Temperature",
    "Commanded Throttle Actuator Control and Relative Throttle position",
    "Fuel Pressure control system",
    "Injection pressure control system",
    "Turbocharger compressor Inlet Pressure",
    "Boost pressure control",
    "Variable Geometry turbo (VGT) control",
    "Wastegate control",
    "Exhaust Pressure",
    "Turbocharger RPM",
    "Turbocharger temperature",
    "Turbocharger temperature",
    "Charge air cooler temperature (CACT)",
    "Exhaust Gas Temperature (EGT) Bank 1",
    "Exhaust Gas Temperature (EGT) Bank 2",
    "Diesel Particulate Filter (DPF)",
    "Diesel Particulate Filter (DPF)",
    "Diesel Particulate Filter (DPF) temperature",
    "NOx NTE(Not-To-Exceed) control area status",
    "PM NTE(Not-To-Exceed) control area status",
    "Engine Run Time [b]",
    "PIDs supported [81-A0]",
    "Engine run time for Auxiliary Emissions Control Device(AECD)",
    "Engine run time for Auxiliary Emissions Control Device(AECD)",
    "NOx Sensor",
    "Manifold Surface Temperature",
    "NOx reagent system",
    "Particulate Matter (PM) sensor",
    "Intake manifold absolute pressure",
    "SCR induce System",
    "Run Time for AECD#11-#15",
    "Run Time for AECD#16-#20",
    "Diesel Aftertreatment",
    "O2 sensor (Wide Range)",
    "Throttle Position G",
    "Engine Friction-Percent Torque",
    "PM Sensor Bank 1 & 2",
    "WWH-OBD Vehicle OBD System Information",
    "WWH-OBD Vehicle OBD System Information",
    "Fuel System Control",
    "WWH-OBD Vehicle OBD Counters support",
    "NOx Warning And Inducement System",
    "Emission Requirements to which vehicle is designed",
    "PIDs supported [61-80]",
    "Driver's demand engine-percent torque",
    "Exhaust Gas Temperature Sensor",
    "Exhaust Gas Temperature Sensor",
    "Hybrid/EV Vehicle System Data, Battery, Voltage",
    "Diesel Exhaust Fluid Sensor Data",
    "O2 Sensor Data",
    "Engine Fuel Rate",
    "Engine Exhaust Flow Rate",
    "Fuel System Percentage Use",
    "PIDs Supported [A1-C0]",
    "NOx Sensor Corrected Data",
    "Cylinder Fuel Rate",
    "Evap System Vapor Pressure",
    "Transmission Actual Gear",
    "Diesel Exhaust Fluid Dosing",
    "Odometer",
    "PIDs supported [C1-E0]",
    "Extra PIDs"
    ]

def format_data_before_write(row):
    locationMap = {}
    locationMap[1] = 1
    locationMap[2] = 2
    locationMap[10] = 3
    locationMap[11] = 4
    locationMap[12] = 5
    locationMap[13] = 6
    locationMap[15] = 7
    locationMap[16] = 8
    locationMap[17] = 9
    locationMap[32] = 10
    locationMap[36] = 11
    locationMap[37] = 12

    extra  = []
    minimumAttributeCount = 259
    attributeCount = 183
    overhead=259 - len(locationMap) - 1
    lst = [""] * attributeCount
    lst[0] = row[1]
    lst[1] = datetime.datetime.fromtimestamp(int(row[2])).strftime("%m/%d/%Y, %H:%M:%S")
    lst[2] = row[3]
    lst[3] = row[4]
    lst[4] = row[5]

    data = row[6]

    incoherentPids = [1, 2, 10, 11, 12, 13, 15, 16, 17, 32, 36, 37]

    if None in chain(*data):
        return lst
    for data_array in data:
        if data_array[1] == None:
            pass
        elif data_array[1] in incoherentPids:
            lst[locationMap[data_array[1]]+4] = data_array[2]
        elif data_array[1] >= minimumAttributeCount:
            lst[data_array[1]-overhead+4] = data_array[2]
        else:
            extra.append([data_array[1], data_array[2]])

    lst[-1] = extra
    return lst


def write_csv_file():
    with open('C:/Users/emlgroup/Desktop/flat_csv_file.csv' , 'w', encoding='UTF8', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(header)

        conn = None
        try:
            conn=psycopg2.connect(user="postgres", password="emso@123", host="localhost", port="5432", database="emso")
            cur=conn.cursor()
            print(conn.get_dsn_parameters(),"\n")
            cur.execute("SELECT * from vehicle_data.location")
            print("The number of rows: ", cur.rowcount)
            row = cur.fetchone()

            while row is not None:
                formatted_row = format_data_before_write(row)
                writer.writerow(formatted_row)
                row=cur.fetchone()
            cur.close()
        except (Exception, psycopg2.DatabaseError) as error:
            print("Error connecting to the Postgres DB")
            print(error)
        finally:
            if conn is not None:
                conn.close()
                print("Postgres connection is now closed")

write_csv_file()