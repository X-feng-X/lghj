curl https://apis.itedus.cn/v1/chat/completions -H "Content-Type: application/json" -H "Authorization: Bearer sk-wtBOjyNviG9NtbYn7f2fF8A2203048Aa86Be6f0f0b824dB9" -d '{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": "1+1"
    }
  ]
}'