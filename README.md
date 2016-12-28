# Oroprise

Recruitment Test on Profiles and MeterReadings REST CRUD operations

## Building for production

To optimize the EventService client for production, run:

    ./mvn clean package

To ensure everything worked, run:

    java -jar target/*.jar
    
    
## REST APIs available
To System test creation of profile,
POST http://localhost:8090/api/profiles/
use requestbody sampleProfileSuccess.json

To System test creation of meterreadings,
POST http://localhost:8090/api/meterreadings/
use requestbody sampleMeterReadingSuccess.json
