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
| `Ship::tooCloseTo` | Imperative Loop (`WhileCanBeForeach`) | Replace Loop with Enhanced For / Stream | 129886 |
| `Compass::charToCompass` | Verbose Conditional (`EnhancedSwitchMigration`) | Replace Conditional with Enhanced Switch | 129886 |
| `Game::jsonShots` | Dead Store (`UnusedAssignment`) | Inline Variable | 129886 |
| `Main::teste2` | Long Parameter List (`unused`) | Introduce Parameter Object | 129886 |
| `Ship::size` | Mutable Field (`FieldMayBeFinal`) | Encapsulate Field / Make Final | 129886 |
| `Game.SaveData::SaveData` | Long Parameter List (`SameParameterValue`) | Remove Parameter / Change Signature | 129886 |
| `Carrack::Carrack` | Duplicate Code (`DuplicateBranchesInSwitch`) | Pull Up Method (para `Ship`) | 129886 |
| `AiShotStrategy::getHitButNotSunkPositions` | Dead Code (`unused`) | Safe Delete | 129861 |
| `Ship::buildShip` | Verbose Conditional (`EnhancedSwitchMigration`) | Replace Conditional with Enhanced Switch | 129861 |
| `BadConstantClass::getConstant` | Constant Method (`SameReturnValue`) | Inline Method / Introduce Constant | 129861 |
| `Main::teste3` | Long Parameter List (`unused`) | Remove Parameter / Change Signature | 129861 |
| `GameSession::getReportEntries` | Mutable Collection Exposure (`Java9CollectionFactory`) | Encapsulate Collection | 129861 |
| `GameReportPdfGenerator::drawCard` | Long Parameter List (`SameParameterValue`) | Introduce Parameter Object | 129861 |
| `Frigate::Frigate` | Duplicate Code (`DuplicateBranchesInSwitch`) | Extract Method | 129861 |