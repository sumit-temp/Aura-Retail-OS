#!/bin/bash

# Aura Retail OS - Documentation Generation Script
# This script generates UML diagrams and converts the design report to PDF

set -e

echo "=========================================="
echo "Aura Retail OS - Documentation Generator"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Create output directories
mkdir -p docs/uml_png
mkdir -p docs/pdf

echo ""
echo -e "${GREEN}✓${NC} Output directories created"

# Check if PlantUML is available
generate_uml() {
    echo ""
    echo "Generating UML diagrams..."
    
    if command -v plantuml &> /dev/null; then
        echo "  Using system PlantUML..."
        plantuml -tpng -o docs/uml_png uml/*.puml
        echo -e "${GREEN}✓${NC} UML diagrams generated with PlantUML"
        return 0
    fi
    
    # Try using PlantUML JAR
    if [ -f "plantuml.jar" ]; then
        echo "  Using local PlantUML JAR..."
        java -jar plantuml.jar -tpng -o docs/uml_png uml/*.puml
        echo -e "${GREEN}✓${NC} UML diagrams generated with PlantUML JAR"
        return 0
    fi
    
    # Try downloading PlantUML
    echo "  PlantUML not found. Attempting to download..."
    if command -v curl &> /dev/null; then
        curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/download/v1.2024.7/plantuml.jar
        java -jar plantuml.jar -tpng -o docs/uml_png uml/*.puml
        echo -e "${GREEN}✓${NC} PlantUML downloaded and diagrams generated"
        return 0
    fi
    
    echo -e "${YELLOW}⚠${NC} PlantUML not available. Skipping PNG generation."
    echo "   You can manually generate diagrams later using:"
    echo "   - Online: https://www.plantuml.com/plantuml/"
    echo "   - Local: Install PlantUML or use the .puml files with your IDE plugin"
    return 1
}

# Generate UML diagrams
generate_uml || true

# Convert Design Report to PDF
echo ""
echo "Converting Design Report to PDF..."

if command -v pandoc &> /dev/null; then
    echo "  Using Pandoc..."
    pandoc docs/Design_Report.md -o docs/pdf/Design_Report.pdf --pdf-engine=pdflatex -V geometry:margin=1in
    echo -e "${GREEN}✓${NC} Design Report converted to PDF with Pandoc"
elif command -v wkhtmltopdf &> /dev/null; then
    echo "  Using wkhtmltopdf..."
    # First convert MD to HTML, then to PDF
    if command -v markdown &> /dev/null; then
        markdown docs/Design_Report.md > docs/Design_Report.html
        wkhtmltopdf docs/Design_Report.html docs/pdf/Design_Report.pdf
        echo -e "${GREEN}✓${NC} Design Report converted to PDF with wkhtmltopdf"
    else
        echo -e "${YELLOW}⚠${NC} markdown command not found. Trying alternative..."
        # Simple conversion using Python
        python3 -c "
import markdown
with open('docs/Design_Report.md', 'r') as f:
    md_content = f.read()
html_content = markdown.markdown(md_content, extensions=['tables', 'fenced_code'])
with open('docs/Design_Report.html', 'w') as f:
    f.write(html_content)
"
        wkhtmltopdf docs/Design_Report.html docs/pdf/Design_Report.pdf
        echo -e "${GREEN}✓${NC} Design Report converted to PDF"
    fi
else
    echo -e "${YELLOW}⚠${NC} No PDF converter found (pandoc or wkhtmltopdf)."
    echo "   The Design Report is available in Markdown format at: docs/Design_Report.md"
    echo ""
    echo "   To convert to PDF, you can:"
    echo "   1. Install Pandoc: sudo apt install pandoc texlive"
    echo "   2. Use online converter: https://pandoc.org/try/"
    echo "   3. Print to PDF from browser: Open Design_Report.md in a Markdown viewer"
fi

# Generate ASCII versions of UML diagrams for quick viewing
echo ""
echo "Generating ASCII versions of UML diagrams..."

if command -v plantuml &> /dev/null || [ -f "plantuml.jar" ]; then
    PLANTUML_CMD="plantuml"
    [ -f "plantuml.jar" ] && PLANTUML_CMD="java -jar plantuml.jar"
    
    $PLANTUML_CMD -tutxt -o docs/uml_png uml/class_diagram.puml 2>/dev/null || true
    $PLANTUML_CMD -tutxt -o docs/uml_png uml/sequence_diagram.puml 2>/dev/null || true
    $PLANTUML_CMD -tutxt -o docs/uml_png uml/subsystem_architecture.puml 2>/dev/null || true
    echo -e "${GREEN}✓${NC} ASCII diagrams generated"
else
    echo -e "${YELLOW}⚠${NC} Skipping ASCII generation (PlantUML not available)"
fi

# Summary
echo ""
echo "=========================================="
echo "Documentation Generation Complete!"
echo "=========================================="
echo ""
echo "Generated files:"
echo "  📄 docs/Design_Report.md          - Design Report (Markdown)"
[ -f "docs/pdf/Design_Report.pdf" ] && echo "  📕 docs/pdf/Design_Report.pdf       - Design Report (PDF)"
[ -f "docs/uml_png/class_diagram.png" ] && echo "  🖼️  docs/uml_png/class_diagram.png    - Class Diagram (PNG)"
[ -f "docs/uml_png/sequence_diagram.png" ] && echo "  🖼️  docs/uml_png/sequence_diagram.png - Sequence Diagram (PNG)"
[ -f "docs/uml_png/subsystem_architecture.png" ] && echo "  🖼️  docs/uml_png/subsystem_architecture.png - Subsystem Architecture (PNG)"
echo "  📝 uml/class_diagram.puml         - Class Diagram (PlantUML source)"
echo "  📝 uml/sequence_diagram.puml      - Sequence Diagram (PlantUML source)"
echo "  📝 uml/subsystem_architecture.puml - Subsystem Architecture (PlantUML source)"
echo ""
echo "To view PlantUML diagrams:"
echo "  - Install PlantUML: https://plantuml.com/starting"
echo "  - Use IDE plugins (IntelliJ, VS Code)"
echo "  - Online editor: https://www.plantuml.com/plantuml/"
echo ""
echo -e "${GREEN}All critical documentation deliverables are now complete!${NC}"
