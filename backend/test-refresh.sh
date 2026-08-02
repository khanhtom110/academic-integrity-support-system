curl -X 'POST' \
  'http://localhost:8080/api/v1/auth/refresh' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJob2FuZ3RoYW5oZGlldTIwMDZAZ21haWwuY29tIiwiaXNzIjoiaGF1aS1sbXMiLCJleHAiOjE3MjMwNjMyODMsImlhdCI6MTcyMjU0NDg4M30.ILRWS5KAjarmpPnlSqi_iMonLmYE-FzbOuy-7H_9zns"
}'
