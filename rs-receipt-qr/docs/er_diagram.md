# Entity-Relationship Diagram (Updated for Tags)

```mermaid
erDiagram
    user {
        int user_id PK
        text first_name
        text last_name
    }
    receipt_raw {
        int receipt_raw_id PK
        int user_id FK
        text url
        int receipt_id FK
    }
    receipt {
        int receipt_id PK
        int user_id FK
        int shop_point_id FK nullable
        datetime pos_time
        real total
        text image_path nullable
        datetime created_at
        text[] tags
    }
    shop_point {
        int shop_point_id PK
        int shop_id FK
        int tax_id
        text name
        text location_name
        text address
        text city
        text city_unit
    }
    shop {
        int shop_id PK
        text name
        text[] tags
    }
    category {
        int category_id PK
        text name unique
    }
    purchase_item {
        int purchase_item_id PK
        int receipt_id FK
        text name
        int category_id FK nullable
        real price
        int quantity
        int warranty_id FK nullable
        text[] tags
    }
    warranty {
        int warranty_id PK
        datetime purchase_date
        int period_months
        text status
        text notes nullable
    }
    
    receipt ||--o{ purchase_item : contains
    shop ||--o{ receipt : issues
    category ||--o{ purchase_item : classifies
    warranty ||--o{ purchase_item : covers
``` 
