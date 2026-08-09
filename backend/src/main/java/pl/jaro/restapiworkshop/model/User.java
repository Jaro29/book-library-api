package pl.jaro.restapiworkshop.model;

import lombok.*;

@Getter
@Setter
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;

    private String displayName;
    private String password;
    private String email;

}
