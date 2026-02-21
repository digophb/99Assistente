# 99 Assistente - App Android para Motoristas

Assistente automático para análise e aceite de corridas do aplicativo **99 Motorista**.

## Funcionalidades

- ✅ Leitura automática das informações da corrida
- ✅ Análise de valor por km
- ✅ Aceite automático baseado em critérios configuráveis
- ✅ Ícone flutuante para acesso rápido
- ✅ Estatísticas de corridas aceitas/rejeitadas

## Configurações Disponíveis

- **Valor mínimo por Km** - Aceita corridas que paguem acima deste valor
- **Distância máxima para busca** - Limita corridas com passageiros muito distantes
- **Auto-aceitar** - Liga/desliga a automação

## Requisitos

- Android 7.0 (API 24) ou superior
- Permissão de Acessibilidade
- Permissão de Sobreposição de Tela

## Instalação

1. Instale o APK
2. Abra o app e configure seus critérios
3. Ative a permissão de Acessibilidade
4. Ative a permissão de Sobreposição de Tela
5. Pressione "Ativar Assistente"

## Como Usar

1. Mantenha o app 99 Motorista aberto
2. O assistente detectará automaticamente as corridas
3. Corridas que atendem aos critérios serão aceitas automaticamente
4. O ícone flutuante mostra o status (verde = ativo, cinza = inativo)

## Observações

- O app não interfere em outras funções do celular
- Funciona apenas quando o 99 Motorista está aberto
- Todas as decisões são registradas para seu controle

## Compilação

Para compilar o projeto:

```bash
./gradlew assembleDebug
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`
