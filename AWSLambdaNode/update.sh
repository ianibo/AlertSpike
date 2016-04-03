zip -r ./FindSubsForAlert.zip ./FindSubsForAlert.js ./node_modules

aws lambda update-function-code \
          --function-name FindSubsForAlert \
          --zip-file "fileb://./FindSubsForAlert.zip"


