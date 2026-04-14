### Abstract

With the accelerating pace of social life, mental health issues have become increasingly prominent, while traditional psychological counseling resources are unevenly distributed and difficult to access. This project designed and developed an intelligent mental health intervention system to address this challenge.

The system adopts a front-end and back-end separation architecture, with Spring Boot backend providing RESTful services and Vue frontend implementing responsive interfaces. The data persistence layer uses MySQL combined with MyBatis-Plus. Core features include: integrating standardized assessment tools like SDS depression scale with dynamic rendering and automatic scoring; designing a dynamic health score evaluation mechanism that triggers crisis warnings when scores fall below thresholds; and developing a mutual aid community module with background management system.

The key innovation lies in integrating large language model APIs for multi-role AI companion dialogues. A three-layer hybrid memory architecture combining Core Memory, sliding window, and pseudo-retrieval augmentation is proposed, effectively solving context forgetting in long conversations. System testing confirms stable operation across all modules, smooth AI responses, and timely warning triggers, demonstrating good practicality and promotion prospects.

**Keywords**: Mental Health System; Large Language Model; Intelligent Intervention; Spring Boot; Vue