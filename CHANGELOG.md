# Change Log
All notable changes to Serene will be documented here. This change log mostly follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## Unreleased

### Changed
- Replaced Boot with the Clojure CLI and `tools.build`. CI tests Clojure on JDK 25 and ClojureScript on Node.js 24.
- Updated to Clojure 1.12. CI tests with ClojureScript 1.12, but ClojureScript consumers must declare their own compiler dependency.

### Added
- Added an opt-in introspection query that retains deprecated inputs on servers that support them.

### Fixed
- Alias maps can omit entries or specify multiple aliases. Aliases work alongside `:prefix`.
- Introspection accepts the `VARIABLE_DEFINITION` directive location.

## 0.0.2 - 2019-01-12

### Fixed
- Fixed `Method code too large!` bug

## 0.0.1 - 2018-11-29
- Initial release

