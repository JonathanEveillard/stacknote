### Entity Design
- Subject contains
  - name
  - color/icon
  - owner
- Topic contains
  - name
  - parent container (subject)
- StudySession
  - start time
  - end time
  - studied topic
- LearningGoal
  - target
  - deadline
  - task list

---

- User -> many subjects
- Subject (belongs to one User) -> many topics
- Topic (belongs to one Subject)-> many notes
- StudySession(belongs to one User) -> ???