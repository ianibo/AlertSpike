zip ./FindSubsForAlert.zip ./FindSubsForAlert.js 

aws lambda create-function \
          --function-name FindSubsForAlert \
          --runtime nodejs \
          --role "arn:aws:iam::603029492791:role/lambda" \
          --handler handler \
          --zip-file "fileb://./FindSubsForAlert.zip"


