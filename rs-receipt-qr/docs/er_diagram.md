# Entity-Relationship Diagram (Updated for Tags)

```mermaid
erDiagram
    receipt {
        int id PK
        int shop_id FK nullable
        datetime date
        real total
        text image_path nullable
        datetime created_at
        text[] tags
    }
    shop {
        int id PK
        text name
        text address nullable
        text phone nullable
    }
    category {
        int id PK
        text name unique
    }
    purchase_item {
        int id PK
        int receipt_id FK
        text name
        int category_id FK nullable
        real price
        int quantity
        int warranty_id FK nullable
        text[] tags
    }
    warranty {
        int id PK
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