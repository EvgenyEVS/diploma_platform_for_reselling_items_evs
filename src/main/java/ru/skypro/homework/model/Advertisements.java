package ru.skypro.homework.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "author")
@EqualsAndHashCode(of = "pk")


public class Advertisements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk", nullable = false)
    @Schema(description = "id объявления", example = "100")
    private Integer pk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", nullable = false, length = 32)
    @Size(min = 4, max = 32)
    private String title;

    @Column(name = "price", nullable = false)
    @Min(0)
    @Max(10000000)
    @Schema(description = "цена объявления", example = "5000")
    private Integer price;

    @Column(name = "image")
    @Schema(description = "ссылка на картинку объявления", example = "/images/ads/1.jpg")
    private String image;

    @Column(name = "description", nullable = false)
    @Size(min = 8, max = 64)
    @Schema(description = "описание объявления", example = "iPhone 100500")
    private String description;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

}
