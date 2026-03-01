USE [SWD392]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[orders](
[order_id] [int] IDENTITY(1,1) NOT NULL,
[buyer_id] [int] NOT NULL,
[order_date] [datetimeoffset](7) NULL,
[status] [nvarchar](20) NOT NULL,
[total_amount] [decimal](10, 2) NOT NULL,
 CONSTRAINT [PK_orders] PRIMARY KEY CLUSTERED 
(
[order_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[orders] ON
INSERT [dbo].[orders] ([order_id], [buyer_id], [order_date], [status], [total_amount]) VALUES (1, 1, CAST(N'2026-02-03T17:05:14.9518073+00:00' AS DateTimeOffset), N'PENDING', CAST(1249000.00 AS Decimal(10, 2)))
INSERT [dbo].[orders] ([order_id], [buyer_id], [order_date], [status], [total_amount]) VALUES (2, 2, CAST(N'2026-02-03T17:05:14.9518073+00:00' AS DateTimeOffset), N'CONFIRMED', CAST(2749000.00 AS Decimal(10, 2)))
INSERT [dbo].[orders] ([order_id], [buyer_id], [order_date], [status], [total_amount]) VALUES (3, 1, CAST(N'2026-02-03T17:05:14.9518073+00:00' AS DateTimeOffset), N'COMPLETED', CAST(499000.00 AS Decimal(10, 2)))
INSERT [dbo].[orders] ([order_id], [buyer_id], [order_date], [status], [total_amount]) VALUES (7, 1, CAST(N'2026-02-03T17:05:14.9518073+00:00' AS DateTimeOffset), N'PENDING', CAST(1249000.00 AS Decimal(10, 2)))
INSERT [dbo].[orders] ([order_id], [buyer_id], [order_date], [status], [total_amount]) VALUES (8, 2, CAST(N'2026-02-03T17:05:14.9518073+00:00' AS DateTimeOffset), N'CONFIRMED', CAST(2749000.00 AS Decimal(10, 2)))
SET IDENTITY_INSERT [dbo].[orders] OFF
GO
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [FK_orders_buyer] FOREIGN KEY([buyer_id])
REFERENCES [dbo].[users] ([user_id])
GO
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [FK_orders_buyer]
GO
ALTER TABLE [dbo].[orders]  WITH CHECK ADD  CONSTRAINT [CK_orders_status] CHECK  (([status]='COMPLETED' OR [status]='CANCELLED' OR [status]='CONFIRMED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[orders] CHECK CONSTRAINT [CK_orders_status]
GO