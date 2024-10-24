package dev.realtards.wzsnacknbites.services;

import dev.realtards.wzsnacknbites.dtos.account.*;
import dev.realtards.wzsnacknbites.exceptions.AccountExistsException;
import dev.realtards.wzsnacknbites.exceptions.AccountNotFoundException;
import dev.realtards.wzsnacknbites.exceptions.InvalidPasswordException;
import dev.realtards.wzsnacknbites.exceptions.PasswordMismatchException;
import dev.realtards.wzsnacknbites.models.Account;
import dev.realtards.wzsnacknbites.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AccountServiceImpl accountService;

	private Account testAccount;
	private AccountRegistrationDto testRegistrationDto;
	private List<Long> idIterable;
	private ListIterator<Long> idIterator;

	@BeforeEach
	void setUp() {
		idIterable = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L);
		idIterator = idIterable.listIterator();

		testAccount = Account.builder()
			.accountId(idIterator.next())
			.fullName("Test User")
			.email("test@example.com")
			.password("password123")
			.privilege(Account.Privilege.USER)
			.build();

		testRegistrationDto = new AccountRegistrationDto();
		testRegistrationDto.setFullName("Test User");
		testRegistrationDto.setEmail("test@example.com");
		testRegistrationDto.setPassword("password123");
	}

	@Test
	void getAllAccounts_ShouldReturnAllAccounts() {
		// Arrange
		List<Account> expectedAccounts = Arrays.asList(testAccount);
		when(accountRepository.findAll()).thenReturn(expectedAccounts);

		// Act
		List<Account> actualAccounts = accountService.getAllAccounts();

		// Assert
		assertThat(actualAccounts).isEqualTo(expectedAccounts);
		verify(accountRepository).findAll();
	}

	@Test
	void createAccount_WithNewEmail_ShouldCreateAccount() {
		// Arrange
		when(accountRepository.existsByEmail(anyString())).thenReturn(false);
		when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

		// Act
		Account createdAccount = accountService.createAccount(testRegistrationDto);

		// Assert
		assertThat(createdAccount).isNotNull();
		assertThat(createdAccount.getEmail()).isEqualTo(testRegistrationDto.getEmail());
		verify(accountRepository).existsByEmail(testRegistrationDto.getEmail());
		verify(accountRepository).save(any(Account.class));
	}

	@Test
	void createAccount_WithExistingEmail_ShouldThrowException() {
		// Arrange
		when(accountRepository.existsByEmail(anyString())).thenReturn(true);

		// Act & Assert
		assertThrows(AccountExistsException.class, () ->
			accountService.createAccount(testRegistrationDto)
		);
		verify(accountRepository).existsByEmail(testRegistrationDto.getEmail());
		verify(accountRepository, never()).save(any(Account.class));
	}

	@Test
	void getAccount_WithExistingId_ShouldReturnAccount() {
		// Arrange
		when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));

		// Act
		Account foundAccount = accountService.getAccount(1L);

		// Assert
		assertThat(foundAccount).isEqualTo(testAccount);
		verify(accountRepository).findById(1L);
	}

	@Test
	void getAccount_WithNonExistingId_ShouldThrowException() {
		// Arrange
		when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () ->
			accountService.getAccount(1L)
		);
		verify(accountRepository).findById(1L);
	}

	@Test
	void updateAccount_WithExistingAccount_ShouldUpdate() {
		// Arrange
		Account updatedAccount = Account.builder()
			.accountId(idIterator.next())
			.fullName("Updated Name")
			.build();
		when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
		when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

		// Act
		Account result = accountService.updateAccount(updatedAccount.getAccountId(), AccountPutDto.fromEntity(updatedAccount));

		// Assert
		assertThat(result.getFullName()).isEqualTo("Updated Name");
		verify(accountRepository).findById(anyLong());
		verify(accountRepository).save(any(Account.class));
	}

	@Test
	void deleteAccount_ShouldDeleteAccount() {
		// Arrange
		doNothing().when(accountRepository).deleteById(anyLong());

		// Act
		accountService.deleteAccount(1L);

		// Assert
		verify(accountRepository).deleteById(1L);
	}

	@Test
	void patchAccount_WithExistingEmail_ShouldPatchAccount() {
		// Arrange
		final Long persistentId = idIterator.next();

		Account existingAccount = Account.builder()
			.accountId(persistentId)
			.fullName("Test User")
			.email("patch@test.com")
			.build();

		Account updatedAccount = Account.builder()
			.accountId(persistentId)
			.fullName("Test User")
			.email("patched@test.com")
			.build();

		// Mock findById and save
		when(accountRepository.findById(persistentId)).thenReturn(Optional.of(existingAccount));
		when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

		// Act
		Account result = accountService.patchAccount(persistentId, AccountPatchDto.fromEntity(updatedAccount));

		// Assert
		assertThat(result.getEmail()).isEqualTo(updatedAccount.getEmail());
		verify(accountRepository).findById(persistentId);
		verify(accountRepository).save(any(Account.class));
	}

	@Test
	void patchAccount_WithExistingEmail_ShouldThrowException() {
		// Arrange
		final Long persistentId = idIterator.next();

		Account existingAccount = Account.builder()
			.accountId(persistentId)
			.fullName("Test User")
			.email("patch@test.com")
			.build();

		AccountPatchDto patchDto = AccountPatchDto.builder()
			.email("patched@test.com")
			.build();

		// Mock findById to return our existing account
		when(accountRepository.findById(persistentId)).thenReturn(Optional.of(existingAccount));
		// Mock existsByEmail to return true (email already taken by another account)
		when(accountRepository.existsByEmail(patchDto.getEmail())).thenReturn(true);

		// Act & Assert
		assertThrows(AccountExistsException.class, () ->
			accountService.patchAccount(persistentId, patchDto)
		);

		// Verify
		verify(accountRepository).findById(persistentId);
		verify(accountRepository).existsByEmail(patchDto.getEmail());
		verify(accountRepository, never()).save(any(Account.class)); // Should never reach save
	}

	@Test
	void patchAccount_WithNullFields_ShouldNotUpdateNullFields() {
		// Arrange
		final Long persistentId = idIterator.next();

		Account existingAccount = Account.builder()
			.accountId(persistentId)
			.fullName("Original Name")
			.email("original@test.com")
			.phone("123456")
			.build();

		AccountPatchDto patchDto = AccountPatchDto.builder()
			.fullName("New Name")
			.email(null)  // Should not update
			.phone(null)  // Should not update
			.build();

		when(accountRepository.findById(persistentId)).thenReturn(Optional.of(existingAccount));
		when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

		// Act
		Account result = accountService.patchAccount(persistentId, patchDto);

		// Assert
		assertThat(result.getFullName()).isEqualTo("New Name");
		assertThat(result.getEmail()).isEqualTo("original@test.com");
		assertThat(result.getPhone()).isEqualTo("123456");
	}

	@Test
	void updatePassword_ShouldUpdatePassword() {
		// Arrange
		final Long persistentId = idIterator.next();
		String encodedOldPassword = "encodedOldPassword";
		String encodedNewPassword = "encodedNewPassword";

		Account testAccount = Account.builder()
			.accountId(persistentId)
			.password(encodedOldPassword)
			.build();

		when(accountRepository.findById(persistentId)).thenReturn(Optional.of(testAccount));
		when(passwordEncoder.encode(anyString())).thenReturn(encodedNewPassword);
		when(passwordEncoder.matches(anyString(), eq(encodedOldPassword))).thenReturn(true);

		// Act
		accountService.updatePassword(persistentId, new PasswordUpdateDto(
			"oldPassword",
			"newPassword",
			"newPassword"
		));

		// Assert
		verify(passwordEncoder).matches(anyString(), eq(encodedOldPassword));
		verify(passwordEncoder).encode(anyString());
		verify(accountRepository).save(argThat(account ->
			account.getPassword().equals(encodedNewPassword)
		));
	}

	@Test
	void updatePassword_WithIncorrectCurrentPassword_ShouldThrowException() {
		// Arrange
		PasswordUpdateDto dto = new PasswordUpdateDto(
			"wrongPassword",
			"newPassword",
			"newPassword"
		);

		when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

		// Act & Assert
		assertThrows(InvalidPasswordException.class, () ->
			accountService.updatePassword(1L, dto)
		);
	}

	@Test
	void updatePassword_WithMismatchedPasswords_ShouldThrowException() {
		// Arrange
		String rawCurrentPassword = "currentPassword";
		String encodedCurrentPassword = "encodedPassword";

		Account testAccount = Account.builder()
			.accountId(1L)
			.password(encodedCurrentPassword)
			.build();

		PasswordUpdateDto dto = new PasswordUpdateDto(
			rawCurrentPassword,     // Raw current password
			"newPassword",         // New password
			"differentPassword"    // Different confirm password
		);

		when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
		when(passwordEncoder.matches(rawCurrentPassword, encodedCurrentPassword)).thenReturn(true);

		// Act & Assert
		assertThrows(PasswordMismatchException.class, () ->
			accountService.updatePassword(testAccount.getAccountId(), dto)
		);
	}

	@Test
	void updatePrivilege_ShouldUpdatePrivilege() {
		// Arrange
		Account testAccount = Account.builder()
			.accountId(1L)
			.privilege(Account.Privilege.USER)  // Starting privilege
			.build();

		Account updatedAccount = Account.builder()
			.accountId(1L)
			.privilege(Account.Privilege.ADMIN)  // New privilege
			.build();

		// Mock findById and save
		when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
		when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

		// Act
		Account result = accountService.updatePrivilege(testAccount.getAccountId(), new PrivilegeUpdateDto(Account.Privilege.ADMIN));

		// Assert
		assertThat(result.getPrivilege()).isEqualTo(Account.Privilege.ADMIN);
		verify(accountRepository).findById(1L);
		verify(accountRepository).save(any(Account.class));
	}
}