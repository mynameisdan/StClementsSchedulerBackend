StClementsSchedulerBackend
==========================

Java backend of St.Clement's Parent Teacher Meeting System

Responsible for generating the schedule of the parent teacher meeting.
Receives data (parents, teachers, meeting setup, requests) from database or file/stdin (csv).

----
Synopsis of project:
Online interface for parents to log in, using a unique account they are assigned,  to request a meeting with a 
teacher of their choice in the Parent Teacher Meeting Day 
Interface is to be set up for use by the administrator by  indicating the day(s) the meetings will be held in, time 
(begin – end), duration of each meeting, and the teachers that will be available for meeting 
Parents must then log in and indicate which time slot (afternoon vs evening) they’re available in, and the 
teachers they would like to meet 
Instead of a first-come-first-serve basis, the client requested for a randomized scheduler which would take the 
input from parents and create a schedule that would maximize the number of meetings each parents would 
have on average (not enough space to accommodate for the amount of parents and their requests) 
Task carried out by team of four in agile software development setting 
Designed with MVC pattern
Algorithm was based on priority system tied to both parent and teacher entities

----
Parents, and teachers are represented by an instance of a Parent and Teacher object respectively,
with any data associated with the parent/teacher stored.

Schedule itself is represented by a ScheduleTriple object and is used extensively for the scheduling algorithm. 
Final schedule details are stored within Parent and Teacher object which the DB then pulls data from each Parent and Teacher objects.

Strategy class contains algorithm details as well as priority system used for scheduling.

Schedule class is the main class to execute scheduling process.

