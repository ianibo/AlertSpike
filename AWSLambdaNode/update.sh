zip ./FindSubsForAlert.zip ./FindSubsForAlert.js 

aws lambda update-function-code \
          --function-name FindSubsForAlert \
          --zip-file "fileb://./FindSubsForAlert.zip"


