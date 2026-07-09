// @tag:auth
/**
 * Authentication module: Spring Authorization Server federating login through Google.
 *
 * <p>Phase H (ADR 0002, {@code docs/adr/0002-auth-oauth-and-account-linking.md}): OAuth 2.1
 * Authorization Code + PKCE, OIDC/OAuth discovery metadata, the OIDC Dynamic Client Registration
 * endpoint (so ChatGPT can register dynamically), and issuance of our own JWT whose subject /
 * {@code account_id} claim is the resolved {@code app_account} and whose {@code aud} is the MCP
 * resource. The end-user login is federated into Google (Spring Security OAuth2 Login); on success
 * the Google identity is resolved to an {@code app_account} via
 * {@link dev.homeincubator.lngedu.account.AccountService}.
 *
 * <p>Config classes: {@link dev.homeincubator.lngedu.auth.AuthorizationServerConfig} (the AS filter
 * chain + registered-client repository + settings), {@link dev.homeincubator.lngedu.auth.DefaultSecurityConfig}
 * (the login/app filter chain), {@link dev.homeincubator.lngedu.auth.TokenConfig} (RSA JWK source,
 * JWT decoder and the token customizer that stamps {@code account_id}/{@code aud}).
 *
 * <p>Scope note (Phase H only): the existing {@code /api}, {@code /sse}, {@code /mcp} surface stays
 * open here; the resource-server chain that actually protects it arrives in Phase I.
 */
package dev.homeincubator.lngedu.auth;
