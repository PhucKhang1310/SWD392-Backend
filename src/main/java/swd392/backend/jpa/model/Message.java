package swd392.backend.jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType;

    @Column(name = "sender_id")
    private Integer senderId;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "body", nullable = false)
    private String body;

    @NotNull
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

}