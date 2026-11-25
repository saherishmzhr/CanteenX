# Smart Canteen Management System

A full-stack **Java 17 + Spring Boot 3.3.x + Thymeleaf + Spring Security** web application for campus/canteen food ordering, kitchen token management, and admin sales analytics.

## Run Locally (3 Steps)

1. Navigate to the project folder:
   ```bash
   cd smart-canteen
   ```
2. Start the application with the Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(On Windows Command Prompt / PowerShell, use `mvnw.cmd spring-boot:run`)*
3. Open your browser at:
   ```text
   http://localhost:8080
   ```

## Demo Logins

Passwords are BCrypt-hashed on startup via `DataInitializer` (and are also shown with one-click fill buttons on `/login`):

| Role | Username | Password | Landing Route |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` | `/admin/dashboard` |
| **STAFF** | `staff` | `staff123` | `/staff/orders` |
| **STUDENT** | `student` | `student123` | `/student/menu` |

## Switching from H2 to MySQL

By default, the app runs offline with an embedded H2 file database at `./data/smartcanteen.mv.db` (zero setup).

To switch to **MySQL**:
1. Run `src/main/resources/schema-mysql.sql` (or let Hibernate `ddl-auto: update` create tables automatically).
2. Activate the `mysql` profile in one line:
   ```bash
   SPRING_PROFILES_ACTIVE=mysql MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_DATABASE=smart_canteen MYSQL_USER=root MYSQL_PASSWORD=yourpassword ./mvnw spring-boot:run
   ```
   Alternatively, uncomment the MySQL `datasource` block inside `src/main/resources/application.yml`.

## Design & Architecture Notes

- **Exact Schema Alignment**: Table and column names (`users`, `menu_items`, `orders`, `order_details`, `user_id`, `full_name`, `token_no`, `order_time`, `payment_method`, `payment_status`, `unit_price`, etc.) are preserved directly.
- **Transactional Order Placement**: `OrderService.placeOrder(...)` is annotated with `@Transactional`, validates cart items and available `stock`, atomically decrements stock, computes line subtotals and order `total`, and assigns a daily-resetting `token_no` (`1, 2, 3...` per calendar day).
- **Session Cart & CSRF**: Student cart state is stored in the HTTP session (`CANTEEN_CART`), and all `POST` forms include Spring Security CSRF tokens.
- **Stock Restoration on Cancellation**: When staff marks an order `CANCELLED` (from a non-cancelled status), ordered quantities are returned to `menu_items.stock` and cancelled orders are excluded from revenue/popular-item totals.
