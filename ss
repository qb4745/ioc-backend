[33mcommit e3138eefd8689f248a00bb709716098e810ba1d7[m[33m ([m[1;36mHEAD -> [m[1;32mfeature/IOC-021-implementar-login[m[33m, [m[1;31morigin/feature/IOC-021-implementar-login[m[33m)[m
Author: qb4745 <jai.vicencio@duocuc.cl>
Date:   Sun Sep 21 15:38:46 2025 -0300

    chore(gemini): add agent context and tracking files
    
    Adds the complete .gemini/ directory to version control. This includes blueprints, prompts, strategy documents, and other context required for the agent's operation.

[33mcommit 810607b34ea0915d454a79b4d2b8608e8f2172e5[m
Author: qb4745 <jai.vicencio@duocuc.cl>
Date:   Sun Sep 21 02:04:40 2025 -0300

    chore: track .gemini directory
    
    Removes the .gemini/ directory from .gitignore to allow tracking of agent-related files and context.

[33mcommit 3bcc8058d3065f75346e95fde8ea1d39c64ea03c[m
Author: qb4745 <jai.vicencio@duocuc.cl>
Date:   Sun Sep 21 02:02:05 2025 -0300

    feat(ingestion): implement persistence layer and security foundations
    
    Implements the full persistence layer for the data ingestion feature, including all entities, repositories, and integration tests. Also adds the foundational security configuration with role-based access control entities.

[33mcommit 9e018e35995ecd208ffe02b893cf0bd68c744ef9[m
Author: qb4745 <jai.vicencio@duocuc.cl>
Date:   Sun Sep 21 02:01:24 2025 -0300

    feat(security): add role-based access control entities and security config
    
    Adds Permission and Role entities for RBAC. Implements SecurityConfig to handle JWT validation and CORS, and a GlobalExceptionHandler for auth errors.

[33mcommit 21a893577c7ef2ca6a80e133967b9f8472b92e37[m
Author: qb4745 <jai.vicencio@duocuc.cl>
Date:   Sun Sep 21 00:15:20 2025 -0300

    docs: refina plan de implementación de ingesta asincrónica
