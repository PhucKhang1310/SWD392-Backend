USE [SWD392]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[messages](
[message_id] [int] IDENTITY(1,1) NOT NULL,
[conversation_id] [int] NOT NULL,
[sender_type] [nvarchar](20) NOT NULL,
[sender_id] [int] NULL,
[body] [nvarchar](max) NOT NULL,
[sent_at] [datetimeoffset](7) NULL,
 CONSTRAINT [PK_messages] PRIMARY KEY CLUSTERED 
(
[message_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[messages] ON
INSERT [dbo].[messages] ([message_id], [conversation_id], [sender_type], [sender_id], [body], [sent_at]) VALUES (1, 1, N'BUYER', 1, N'Hi, where is my order?', CAST(N'2026-02-03T17:05:14.9553142+00:00' AS DateTimeOffset))
INSERT [dbo].[messages] ([message_id], [conversation_id], [sender_type], [sender_id], [body], [sent_at]) VALUES (2, 1, N'AI', NULL, N'Your order is currently being processed.', CAST(N'2026-02-03T17:05:14.9553142+00:00' AS DateTimeOffset))
INSERT [dbo].[messages] ([message_id], [conversation_id], [sender_type], [sender_id], [body], [sent_at]) VALUES (3, 1, N'STAFF', 4, N'We are checking this for you.', CAST(N'2026-02-03T17:05:14.9553142+00:00' AS DateTimeOffset))
INSERT [dbo].[messages] ([message_id], [conversation_id], [sender_type], [sender_id], [body], [sent_at]) VALUES (4, 2, N'BUYER', 2, N'Thanks, issue resolved.', CAST(N'2026-02-03T17:05:14.9553142+00:00' AS DateTimeOffset))
SET IDENTITY_INSERT [dbo].[messages] OFF
GO
ALTER TABLE [dbo].[messages]  WITH CHECK ADD  CONSTRAINT [FK_messages_conversation] FOREIGN KEY([conversation_id])
REFERENCES [dbo].[conversations] ([conversation_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[messages] CHECK CONSTRAINT [FK_messages_conversation]
GO
ALTER TABLE [dbo].[messages]  WITH CHECK ADD  CONSTRAINT [CK_messages_sender_type] CHECK  (([sender_type]='AI' OR [sender_type]='STAFF' OR [sender_type]='BUYER'))
GO
ALTER TABLE [dbo].[messages] CHECK CONSTRAINT [CK_messages_sender_type]
GO