#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../../.."

# La création du schéma par Hibernate est réservée à cette base locale dédiée.
db_port=${PLAYWRIGHT_DB_PORT:-55432}
http_port=${PLAYWRIGHT_HTTP_PORT:-58080}
smtp_port=${PLAYWRIGHT_SMTP_PORT:-53025}
base_url="http://127.0.0.1:${http_port}"
mkdir -p target/playwright

jetty_maven_opts=${MAVEN_OPTS:-}
if [[ ${PLAYWRIGHT_COVERAGE:-false} == true ]]; then
  mvn -B -ntp -Pcoverage dependency:copy@copy-jacoco-agent
  mkdir -p target/coverage-data
  jetty_maven_opts+=" -javaagent:target/coverage-agent/jacoco-agent.jar=destfile=target/coverage-data/playwright.exec,append=false,includes=fr.univrouen.poste.*"
fi

MAVEN_OPTS="$jetty_maven_opts" mvn -B -ntp compile jetty:run -Pbrowser-tests \
  -Djetty.http.port="$http_port" \
  -Ddatabase.url="jdbc:postgresql://127.0.0.1:${db_port}/dematec_browser_test" \
  -Ddatabase.username=dematec_browser -Ddatabase.password=dematec_browser \
  -Demail.isEnabled=true -Demail.host=127.0.0.1 -Demail.port="$smtp_port" \
  -Demail.protocol=smtp -Demail.username= -Demail.password= \
  -Demail.starttls=false -Demail.ssl=false >target/playwright/jetty.log 2>&1 &
server_pid=$!
trap 'kill "$server_pid" 2>/dev/null || true; wait "$server_pid" 2>/dev/null || true' EXIT

ready=false
for ((attempt = 0; attempt < 120; attempt++)); do
  if ! kill -0 "$server_pid" 2>/dev/null; then
    break
  fi
  if curl --fail --silent --max-time 2 "$base_url/login" | grep 'id="j_username"' >/dev/null; then
    ready=true
    break
  fi
  sleep 1
done

if [[ "$ready" != true ]]; then
  tail -n 100 target/playwright/jetty.log >&2
  exit 1
fi

PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 \
  mvn -B -ntp test -DskipTests=false -Dtest=DematBrowserIT \
  -Dplaywright.baseUrl="$base_url" -Dplaywright.smtpPort="$smtp_port"
