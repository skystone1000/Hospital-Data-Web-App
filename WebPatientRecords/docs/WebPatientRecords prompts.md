# Prompts WebPatientRecords Back up

```
I currently have android app for 
patientRecords: C:\xampp\htdocs\Hospital-Data-Web-App\PatientRecords
Current web app: C:\xampp\htdocs\Hospital-Data-Web-App\Web-App

I plan on making data synchronized for both the 
Mobile app(offline + Sync to update data in firebase when connected to internet) and 
webapp(offline/local + Sync to update data in firebase when connected to internet)

I am refactoring the current php web app to Next.js full-stack webapp in new codebase from scratch 
( at location C:\xampp\htdocs\Hospital-Data-Web-App\WebPatientRecords)
and the plan is in plan_ui_refactor.md
I want to make sure that data is not overwritten by either synchronizing data to firebase.

Update the plan_ui_refactor.md by reading the mobile app and current web app for any further update taking into consideration all above things and ask any clarifying questions if needed
```
-------------------------------------------------
```
Build a detailed documentation for WebPatientRecords in WebPatientRecords/docs and add files ARCHITECTURE.md, CODEBASE.md, FEATURES.md  add rules to update these docs whenever there is an update to architecture or new feature is added or some codebase is added in the specific files. Also add a readme for a new user to get used to the Next.js webapp, how to run and how to get started
```
------------------------------------------------
```
Fix following bugs
1) When a new patient is added the time is not getting added  and same is happening for the add new follow up
2) On adding a new patient, in dashboard Today card data is not getting updated for new patient (could be because of previous issue of time not getting added)
3) On patient details screen (http://localhost:3000/patients/380) the paid amount should show the sum of all the amount (initial payment + all follow ups), same for balance
4) On Patient Details screen, there is an edit button which should be on "Initial Patient Details" section which edits initial details, and also add a edit and delete button on each of the follow ups
5) On Patient Details screen, Delete button on top of screen should delete the patient record including all the follow ups for that patient (Reverify) 
6) On All patients screen, Add filters on basis of first name, last name, registration number, date,etc
7) In side nav, put "add patient" option above "Patients" option
```

