# Git-Workflow

## 1. Ziel des Workflows

Wir arbeiten mit drei Ebenen:

```text
                         ┌─────────────┐
                         │    main     │
                         │  geschützt  │
                         └──────▲──────┘
                                │
                         stabiler Stand
                                │
                         ┌──────┴──────┐
                         │   staging   │
                         │ Integration │
                         └──────▲──────┘
                                │
                         fertiges Feature
                                │
             ┌──────────────────┼──────────────────┐
             │                  │                  │
      feature/ticket-1   feature/ticket-2   feature/ticket-3

```

### `main`

`main` enthält ausschließlich einen stabilen und geprüften Stand des Projekts.

- `main` ist geschützt.
- Es wird nicht direkt auf `main` gepusht.
- Änderungen kommen nur aus `staging`.
- Ein Stand wird erst nach `main` übernommen, wenn wir ihn gemeinsam geprüft haben.

### `staging`

`staging` ist unser gemeinsamer Integrationsstand.

- Hier werden die fertigen Features zusammengeführt.
- `staging` kann neuer sein als `main`.
- Features werden erst nach `staging` übernommen, wenn der Entwickler sie fertiggestellt und vorher mit dem aktuellen `staging`-Stand abgeglichen hat.
- Da wir gemeinsam am Projekt arbeiten, prüfen wir den Stand vor dem Push nach `staging` gemeinsam.

### Aufbau Tickets
`BAL`-`nr`: Beschreibung 
```text
BAL-000: Add Documentary
```

### Feature-Branches

Wir nutzen 

Für jedes Ticket bzw. Feature wird ein eigener Branch erstellt.

Beispiele:

```text
feature/BAL-001
feature/BAL-002
feature/BAL-003
feature/BAL-004

```

In einem Feature-Branch wird ausschließlich an dem jeweiligen Ticket gearbeitet.

---


# 2. Workflow

## Neues Ticket

```bash
git switch staging
git pull
git switch -c feature/ticket-123

```

## Entwickeln

```bash
git status
git diff
git add .
git diff --staged
git commit -m "BAL-000: Beschreibung"

Beim ersten Push:
git push -u origin feature/BAL-000

Danach reicht:
git push

```

Weitere Änderungen:

```bash
git status
git diff
git add .
git diff --staged
git commit -m "BAL-000: Weitere Änderungen"
git push

```

## Feature fertig

```bash
git fetch origin
git rebase origin/staging

```

Bei Konflikten:

```bash
git status
# Konflikte lösen
git add <datei>
git rebase --continue

```

Bei Problemen:

```bash
git rebase --abort

```

Nach erfolgreichem Rebase:

```bash
git push --force-with-lease

```

## Feature nach `staging`

```bash
git switch staging
git pull
git merge feature/ticket-123
git push

```

## Geprüften `staging`-Stand nach `main`

```bash
git switch staging
git pull

git switch main
git pull

git merge staging
git push

```

---

# 3. Unsere wichtigsten Regeln

1. **Nie direkt auf `main` arbeiten.**
2. **Jedes Ticket bekommt einen eigenen Feature-Branch.**
3. **Feature-Branches basieren auf `staging`.**
4. **Vor der Integration immer den aktuellen `staging`-Stand per Rebase übernehmen.**
5. **Konflikte werden im Feature-Branch gelöst.**
6. **Nach einem Rebase mit `--force-with-lease` pushen.**

## Merksatz

> **Feature bauen → mit aktuellem `staging` rebasen → Konflikte im Feature lösen → Feature nach `staging` → gemeinsam testen → stabilen Stand nach `main`.**