#!/bin/bash

# EyeDock Test Execution Scripts
# Uso: ./run-tests.sh [unit|instrumentation|benchmark|lint|all]

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${GREEN}================================${NC}"
    echo -e "${GREEN} $1${NC}"
    echo -e "${GREEN}================================${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
}

# Função para executar testes unitários
run_unit_tests() {
    print_header "Executando Testes Unitários"
    
    ./gradlew clean test --continue || {
        print_error "Testes unitários falharam"
        return 1
    }
    
    print_success "Testes unitários concluídos"
    
    # Gerar relatório de cobertura
    print_header "Gerando Relatório de Cobertura"
    ./gradlew jacocoTestReport
    
    # Verificar gate de cobertura
    ./gradlew jacocoTestCoverageVerification || {
        print_error "Gate de cobertura falhou - mínimo 85% para unit tests"
        return 1
    }
    
    print_success "Cobertura de testes atende aos critérios"
}

# Função para executar testes de instrumentação
run_instrumentation_tests() {
    print_header "Executando Testes de Instrumentação"
    
    # Verificar se há dispositivo/emulador conectado
    if ! adb devices | grep -q "device$"; then
        print_error "Nenhum dispositivo Android conectado"
        print_warning "Inicie um emulador ou conecte um dispositivo"
        return 1
    fi
    
    ./gradlew connectedCheck --continue || {
        print_error "Testes de instrumentação falharam"
        return 1
    }
    
    print_success "Testes de instrumentação concluídos"
}

# Função para executar testes de benchmark
run_benchmark_tests() {
    print_header "Executando Testes de Performance"
    
    # Verificar se há dispositivo conectado
    if ! adb devices | grep -q "device$"; then
        print_error "Dispositivo Android necessário para benchmarks"
        return 1
    }
    
    # Verificar se é build de release
    print_warning "Benchmarks devem ser executados em build Release"
    
    ./gradlew :app:benchmarkRelease || {
        print_error "Testes de benchmark falharam"
        return 1
    }
    
    print_success "Testes de performance concluídos"
}

# Função para executar análise estática
run_lint() {
    print_header "Executando Análise Estática"
    
    # Lint do Android
    ./gradlew lint || {
        print_error "Android Lint falhou"
        return 1
    }
    
    # Detekt
    ./gradlew detekt || {
        print_error "Detekt falhou"
        return 1
    }
    
    # KtLint
    ./gradlew ktlintCheck || {
        print_error "KtLint falhou"
        print_warning "Execute './gradlew ktlintFormat' para corrigir automaticamente"
        return 1
    }
    
    print_success "Análise estática concluída sem problemas"
}

# Função para executar verificação de dependências
run_security_scan() {
    print_header "Executando Verificação de Segurança"
    
    ./gradlew dependencyCheckAnalyze || {
        print_error "Verificação de dependências falhou"
        return 1
    }
    
    print_success "Verificação de segurança concluída"
}

# Função para executar todos os testes
run_all_tests() {
    print_header "Executando Pipeline Completa de Testes"
    
    local failed_tests=()
    
    # Lint primeiro (mais rápido)
    if ! run_lint; then
        failed_tests+=("lint")
    fi
    
    # Testes unitários
    if ! run_unit_tests; then
        failed_tests+=("unit")
    fi
    
    # Security scan
    if ! run_security_scan; then
        failed_tests+=("security")
    fi
    
    # Testes de instrumentação (se dispositivo disponível)
    if adb devices | grep -q "device$"; then
        if ! run_instrumentation_tests; then
            failed_tests+=("instrumentation")
        fi
        
        # Benchmarks apenas se instrumentation passou
        if [[ ! " ${failed_tests[@]} " =~ " instrumentation " ]]; then
            if ! run_benchmark_tests; then
                failed_tests+=("benchmark")
            fi
        fi
    else
        print_warning "Pulando testes de instrumentação - nenhum dispositivo conectado"
    fi
    
    # Relatório final
    echo
    print_header "Relatório Final"
    
    if [ ${#failed_tests[@]} -eq 0 ]; then
        print_success "Todos os testes passaram! ✅"
        return 0
    else
        print_error "Testes falharam: ${failed_tests[*]}"
        return 1
    fi
}

# Função para mostrar relatórios
show_reports() {
    print_header "Abrindo Relatórios de Teste"
    
    local reports_dir="app/build/reports"
    
    if [[ -d "$reports_dir" ]]; then
        echo "Relatórios disponíveis em:"
        echo "📊 Unit Tests: $reports_dir/tests/testDebugUnitTest/index.html"
        echo "📊 Coverage: $reports_dir/jacoco/test/html/index.html"
        echo "📊 Lint: $reports_dir/lint-results-debug.html"
        echo "📊 Detekt: $reports_dir/detekt/detekt.html"
        
        if [[ -d "$reports_dir/androidTests" ]]; then
            echo "📊 Instrumentation: $reports_dir/androidTests/connected/index.html"
        fi
        
        if [[ -d "$reports_dir/benchmark" ]]; then
            echo "📊 Benchmark: $reports_dir/benchmark/"
        fi
    else
        print_warning "Nenhum relatório encontrado. Execute os testes primeiro."
    fi
}

# Função principal
main() {
    local test_type=${1:-"help"}
    
    case $test_type in
        "unit")
            run_unit_tests
            ;;
        "instrumentation")
            run_instrumentation_tests
            ;;
        "benchmark")
            run_benchmark_tests
            ;;
        "lint")
            run_lint
            ;;
        "security")
            run_security_scan
            ;;
        "all")
            run_all_tests
            ;;
        "reports")
            show_reports
            ;;
        "help"|*)
            echo "EyeDock Test Runner"
            echo
            echo "Uso: $0 [comando]"
            echo
            echo "Comandos disponíveis:"
            echo "  unit            Executar testes unitários"
            echo "  instrumentation Executar testes de instrumentação"
            echo "  benchmark       Executar testes de performance"
            echo "  lint            Executar análise estática"
            echo "  security        Executar verificação de segurança"
            echo "  all             Executar pipeline completa"
            echo "  reports         Mostrar localização dos relatórios"
            echo "  help            Mostrar esta ajuda"
            echo
            echo "Exemplos:"
            echo "  $0 unit         # Apenas testes unitários"
            echo "  $0 all          # Pipeline completa"
            echo "  $0 reports      # Ver onde estão os relatórios"
            ;;
    esac
}

# Verificar se estamos no diretório correto
if [[ ! -f "gradlew" ]]; then
    print_error "gradlew não encontrado. Execute este script do diretório raiz do projeto."
    exit 1
fi

# Executar função principal
main "$@"
