package dev.realtards.wzsnacknbites.dtos.account;

import dev.realtards.wzsnacknbites.models.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Partial account update request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountPatchDto {

    @Schema(description = "User's full name", example = "Emilia", minLength = 2, maxLength = 128)
	@Size(min = 2, max = 128, message = "Full name must be between 2 and 128 characters")
	private String fullName;

    @Schema(description = "User's email address", example = "emilia@example.com")
	@Email(message = "Email format is invalid")
	private String email;

    @Schema(description = "User's phone number", example = "+12345678901", pattern = "^\\+?[1-9][0-9]{7,14}$")
    @Pattern(regexp = "^?[1-9][0-9]{7,14}$", message = "Invalid phone number format")
    private String phone;

    private Account.Privilege privilege;

	public AccountPatchDto(Account account) {
		this.fullName = account.getFullName();
		this.email = account.getEmail();
		this.phone = account.getPhone();
		this.privilege = account.getPrivilege();
	}

	public static AccountPatchDto fromEntity(Account account) {
		return new AccountPatchDto(account);
	}
}
