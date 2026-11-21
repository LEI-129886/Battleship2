# Refactoring Opportunities

| Local | Nome do cheiro no código | Nome da refabricação | Número do aluno |
|---|---|---|---|
| `Game::restoreMoves` | Long Method (`ExtractMethodRecommender`) | Extract Method | 125321 |
| `Fleet::colisionRisk` | Boolean Method Is Always Inverted | Invert Boolean | 125321 |
| `Move` | Data Class (`ClassCanBeRecord`) | Convert to Record | 125321 |
| `Position::equals` | Unnecessary Explicit Cast (`PatternVariableCanBeUsed`) | Introduce Pattern Variable | 125321 |
| `Main::teste` | Dead Code (`unused`) | Safe Delete | 125321 |
| `EnvyOfMove` | Feature Envy (`EnvyOfMove` / `unused`) | Move Instance Method | 125321 |
| `Caravel::Caravel` | Duplicate Code (`DuplicateBranchesInSwitch`) | Consolidate Duplicate Conditional Fragments | 125321 |
