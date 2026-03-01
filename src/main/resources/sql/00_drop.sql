-- Drop all tables in reverse foreign-key order before recreating them.
-- Leaf tables (most dependencies) first, root tables (users, products) last.

IF OBJECT_ID('dbo.reviews',       'U') IS NOT NULL DROP TABLE dbo.reviews
IF OBJECT_ID('dbo.messages',      'U') IS NOT NULL DROP TABLE dbo.messages
IF OBJECT_ID('dbo.conversations', 'U') IS NOT NULL DROP TABLE dbo.conversations
IF OBJECT_ID('dbo.cart_items',    'U') IS NOT NULL DROP TABLE dbo.cart_items
IF OBJECT_ID('dbo.order_items',   'U') IS NOT NULL DROP TABLE dbo.order_items
IF OBJECT_ID('dbo.carts',         'U') IS NOT NULL DROP TABLE dbo.carts
IF OBJECT_ID('dbo.orders',        'U') IS NOT NULL DROP TABLE dbo.orders
IF OBJECT_ID('dbo.products',      'U') IS NOT NULL DROP TABLE dbo.products
IF OBJECT_ID('dbo.users',         'U') IS NOT NULL DROP TABLE dbo.users
