# Cofre BTC — APK offline

Aplicativo Android nativo, sem permissão de internet e sem localStorage. O cofre é salvo somente no armazenamento interno privado do aplicativo como `cofre.btc`.

## Criptografia
- JSON da carteira é criptografado integralmente antes de ser salvo.
- AES-256-GCM para confidencialidade + autenticação.
- PBKDF2-HMAC-SHA256 com 600.000 iterações para derivar a chave da senha (com fallback Android antigo para SHA-1).
- Salt e IV aleatórios por gravação.
- O arquivo persistido não contém a seed em texto puro.
- `FLAG_SECURE` bloqueia screenshots/gravação da tela.
- O app não declara permissão INTERNET.
- Bloqueio automático ao sair do app/ir para segundo plano.

## Arquivo do cofre
O conteúdo persistido é uma linha opaca no formato `CBTC1.salt.iv.ciphertext`, toda codificada em Base64. O JSON existe apenas dentro da camada criptografada.

## Limitações importantes
Este projeto é uma base funcional, não uma auditoria de segurança. Para guardar fundos reais, faça revisão independente do código e mantenha backups criptografados. Se esquecer a senha, não há recuperação por design.

O APK compilado não está incluído porque este ambiente não possui Android SDK/Gradle configurado para produzir um binário confiável. O projeto está pronto para abrir no Android Studio e compilar.
