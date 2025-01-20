1. Use postgres database (may use dbeaver or pgadmin)
2. Run the following sql script:
    CREATE DATABASE manulife;
3. right click pom.xml and Build Module 'manulife-vaadin' to download all the dependencies
4. Ensure Manulife.jrxml is in the resources folder.
5. Adjust the username for your dbeaver accordingly in the application.properties at line 5
6. Adjust the password for your dbeaver accordingly in the application.properties at line 6
7. To run it, click the Application file and choose the green arrow that shows "Run 'Application.main()'"