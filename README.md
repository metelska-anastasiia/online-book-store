# 📚 online-book-store 📚

**All endpoints for the project**

Available for non authenticated users:
``` 
POST: /api/auth/register
``` 
Example of request body to **register**:

```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "repeatPassword": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "shippingAddress": "123 Main St, City, Country"
}
```
``` 
POST: /api/auth/login
``` 
Example of request body to **log-in**:

```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```


Available for users with role USER
``` 
GET: /api/books

GET: /api/books/{id} 

GET: /api/categories

GET: /api/categories/{id}

GET: /api/categories/{id}/books

GET: /api/cart

POST: /api/cart

PUT: /api/cart/cart-items/{cartItemId}

DELETE: /api/cart/cart-items/{cartItemId}

GET: /api/orders

POST: /api/orders

GET: /api/orders/{orderId}/items

GET: /api/orders/{orderId}/items/{itemId}
``` 

Available for users with role ADMIN
``` 
POST: /api/books/

PUT: /api/books/{id}

DELETE: /api/books/{id}

POST: /api/categories

PUT: /api/categories/{id}

DELETE: /api/categories/{id}

PATCH: /api/orders/{id}
``` 

