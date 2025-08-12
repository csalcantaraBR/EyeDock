# 📚 Documentação EyeDock

> **Documentação completa do sistema de monitoramento de câmeras IP**

## 📖 Índice da Documentação

### 🏗️ Arquitetura
Documentação sobre a arquitetura e design do sistema.

- [`functional-requirements.md`](architecture/functional-requirements.md) - Requisitos funcionais detalhados
- [`ux-requirements.md`](architecture/ux-requirements.md) - Requisitos de experiência do usuário
- [`technical-requirements.md`](architecture/technical-requirements.md) - Requisitos técnicos

### ⚙️ Setup & Configuração
Guias de configuração e setup do ambiente.

- [`google-cloud-setup.md`](setup/google-cloud-setup.md) - Configuração do Google Cloud
- [`google-drive-setup.md`](setup/google-drive-setup.md) - Configuração do Google Drive
- [`local-testing-setup.md`](setup/local-testing-setup.md) - Setup para testes locais

### 💻 Desenvolvimento
Documentação para desenvolvedores.

- [`tdd-guide.md`](development/tdd-guide.md) - Guia de Test-Driven Development
- [`tdd-final-summary.md`](development/tdd-final-summary.md) - Resumo final do TDD
- [`tdd-progress.md`](development/tdd-progress.md) - Progresso do TDD
- [`implementation-plan.md`](development/implementation-plan.md) - Plano de implementação
- [`implementation-summary.md`](development/implementation-summary.md) - Resumo da implementação
- [`implementation-progress-summary.md`](development/implementation-progress-summary.md) - Progresso da implementação
- [`refactoring-summary.md`](development/refactoring-summary.md) - Resumo do refactoring

### 🧪 Testes
Documentação sobre testes e qualidade.

- [`complete-testing-guide.md`](testing/complete-testing-guide.md) - Guia completo de testes
- [`test-suite-readme.md`](testing/test-suite-readme.md) - README da suíte de testes
- [`tests.md`](testing/tests.md) - Documentação dos testes

### 🚀 Deploy & CI/CD
Documentação sobre deployment e integração contínua.

- [`deployment-pipeline.md`](deployment/deployment-pipeline.md) - Pipeline de deployment
- [`deployment-checklist.md`](deployment/deployment-checklist.md) - Checklist de deployment
- [`github-actions-summary.md`](deployment/github-actions-summary.md) - Resumo do GitHub Actions
- [`github-actions-todo.md`](deployment/github-actions-todo.md) - TODO do GitHub Actions

### 📋 Compliance & Políticas
Documentação sobre compliance e políticas.

- [`compliance-audit.md`](compliance/compliance-audit.md) - Auditoria de compliance
- [`privacy-policy.md`](compliance/privacy-policy.md) - Política de privacidade
- [`play-store-listing.md`](compliance/play-store-listing.md) - Listagem na Play Store

## 🔍 Como Usar Esta Documentação

### Para Desenvolvedores
1. Comece com [`architecture/functional-requirements.md`](architecture/functional-requirements.md)
2. Leia [`development/tdd-guide.md`](development/tdd-guide.md) para entender a metodologia
3. Configure o ambiente com os guias em [`setup/`](setup/)
4. Execute os testes seguindo [`testing/complete-testing-guide.md`](testing/complete-testing-guide.md)

### Para DevOps
1. Configure o CI/CD com [`deployment/github-actions-summary.md`](deployment/github-actions-summary.md)
2. Siga o checklist em [`deployment/deployment-checklist.md`](deployment/deployment-checklist.md)
3. Configure o pipeline em [`deployment/deployment-pipeline.md`](deployment/deployment-pipeline.md)

### Para Compliance
1. Revise [`compliance/compliance-audit.md`](compliance/compliance-audit.md)
2. Configure a política de privacidade em [`compliance/privacy-policy.md`](compliance/privacy-policy.md)
3. Prepare a listagem da Play Store em [`compliance/play-store-listing.md`](compliance/play-store-listing.md)

## 📝 Convenções

### Estrutura dos Arquivos
- **kebab-case**: Todos os nomes de arquivos usam kebab-case
- **Organização por categoria**: Arquivos organizados por área de conhecimento
- **Links relativos**: Todos os links são relativos para facilitar navegação

### Formatação
- **Markdown**: Todos os arquivos usam Markdown
- **Emojis**: Uso consistente de emojis para categorização visual
- **Badges**: Badges para status e informações importantes

## 🔄 Manutenção

### Atualizações
- Documentação atualizada automaticamente via CI/CD
- Revisão mensal de todos os documentos
- Versionamento semântico para mudanças importantes

### Contribuição
Para contribuir com a documentação:

1. Crie uma branch para sua contribuição
2. Siga as convenções de formatação
3. Atualize este índice se necessário
4. Abra um Pull Request

---

**Última atualização**: Dezembro 2024  
**Versão**: 1.0.0  
**Mantido por**: Equipe EyeDock
