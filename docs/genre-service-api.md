# Genre Service API

The **Genre Service** is responsible for managing book genres within the
*Book Fair Reservation Management System*.\
It provides CRUD operations along with user-tracking through the
**user_snapshot** table.

## Base URLs

  Environment                URL
  -------------------------- ---------------------------------------
  **Local (Gateway)**        `http://localhost:8080`
  **Local Direct Service**   `http://localhost:8082`

## Features

-   Create, read, update, and delete genres\
-   Prevent duplicate genre **name** and **code**\
-   Validate required fields\
-   Track createdBy & updatedBy using **user_snapshot**\
-   Fetch all genres created by a specific user\
-   Global exception handling (400 / 404 / 409 errors)

## API Endpoints

### **1. Create Genre**

`POST /genres`

#### Request Body

``` json
{
  "code": "FAN",
  "name": "Fantasy",
  "description": "Fantasy books",
  "displayOrder": 1,
  "isActive": true,
  "userId": "11111111-1111-1111-1111-111111111111"
}
```
#### success Response

``` json
{
  "id": "uuid",
  "code": "FAN",
  "name": "Fantasy",
  "description": "Fantasy books",
  "displayOrder": 1,
  "isActive": true,
  "createdAt": "2025-11-23T10:00:00",
  "updatedAt": "2025-11-23T10:00:00"
}
```

### **2. Get All Genres**

`GET /genres`

### **3. Get Genre by ID**

`GET /genres/{id}`

### **4. Update Genre**

`PUT /genres/{id}`

#### Request Body

``` json
{
  "code": "FAN",
  "name": "Fantasy Updated",
  "description": "Updated description",
  "displayOrder": 2,
  "isActive": true,
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

### **5. Delete Genre**

`DELETE /genres/{id}`

### **6. Get Genres by User ID**

`GET /genres/user/{userId}`

## Database Tables Used

### **genre**

Stores genre details.

### **user_snapshot**

Stores snapshot of user info like: - user_id\
- first_name\
- last_name\
- email\
- role\
- status

## Error Handling

  Error Type                       HTTP Code   When It Happens
  -------------------------------- ----------- ------------------------------
  **BadRequestException**          400         missing fields, invalid data
  **ResourceNotFoundException**    404         genre/user not found
  **DuplicateResourceException**   409         name or code already exists

## Notes

-   `userId` is required for create & update (audit purpose).
-   All endpoints return clean JSON responses.
