name: Fix Java Code Automatically

on:
  push:
    branches:
      - main

jobs:
  fix-code:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up Java
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'temurin' # הגדרה חובה

    - name: Read existing Java code
      id: read-code
      run: |
        CODE=$(cat Main.java)
        echo "::set-output name=code::$CODE"

    - name: Fix code using OpenAI API
      run: |
        curl -X POST "https://api.openai.com/v1/completions" \
        -H "Authorization: Bearer ${{ secrets.OPENAI_API_KEY }}" \
        -H "Content-Type: application/json" \
        -d "{
          \"model\": \"text-davinci-003\",
          \"prompt\": \"Fix this Java code:\n${{ steps.read-code.outputs.code }}\",
          \"max_tokens\": 300,
          \"temperature\": 0.2
        }" > fixed_code.json

    - name: Save fixed code
      run: |
        cat fixed_code.json | jq -r '.choices[0].text' > Main.java
        cat Main.java

    - name: Compile and run fixed code
      run: |
        javac Main.java && java Main || echo "Compilation failed!"

    - name: Commit and push fixed code
      if: success()
      run: |
        git config --global user.name "GitHub Actions"
        git config --global user.email "actions@github.com"
        git add Main.java
        git commit -m "Fix Java code using OpenAI"
        git push
