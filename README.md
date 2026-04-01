# CircleToSearch for Aido

This repository contains the standalone "Circle to Search" feature integration for the Aido project. 

## 🚀 Features
- **Visual Search Overlay**: A transparent activity that allows users to capture and interact with screen content.
- **OCR Integration**: Powered by Google ML Kit to identify and extract text from any part of the screen.
- **Multi-Engine Support**: Seamlessly search using Google Lens, Bing, Yandex, Perplexity, ChatGPT, and more.
- **Smart Text Actions**: Perform AI-driven actions (Smart Reply, Tone Rewrite) directly on selected screen text.
- **Customizable UI**: Support for Dark Mode, gradient borders, and desktop/mobile search modes.

## 📂 Project Structure
- **/app/src/main/java/com/rr/aido/ui/circletosearch/**: Full implementation code.
  - `OverlayActivity.kt`: The main entry point for the visual search overlay.
  - `data/`: Bitmaps, Text nodes, and Search Engine definitions.
  - `ui/`: Compose-based UI components (CircleToSearchScreen, SearchOverlay, Bottom/Top Bars).
  - `utils/`: Image processing, UI preferences, and search engine link generation.

## 🛠️ Requirements
- Android SDK 33+ (Recommended)
- Google ML Kit (Text Recognition)
- Internet connection for search engine queries

## 📜 Usage
The feature is triggered via the Aido accessibility service. Once active, a screenshot is taken, and the user can:
1. **Circle/Select** an area to search for an image.
2. **Double-tap** text to extract it and search or use AI triggers.
3. **Toggle** between different search engines in the bottom sheet.
