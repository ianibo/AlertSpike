
#echo Publish polygon alert
#aws sns publish --topic-arn "arn:aws:sns:eu-west-1:603029492791:CAPAlertEvent" --message "{ \"polygonCoordinates\": [ [-109.5297,40.4554], [-109.5298,40.4556], [-109.5299,40.4556], [-109.5299,40.4554], [-109.5297,40.4554] ] }"
#
#sleep 1
#
#echo Publish circle alert
#aws sns publish --topic-arn "arn:aws:sns:eu-west-1:603029492791:CAPAlertEvent" --message "{ \"circleCenterRadius\": [ -109.5288, 40.4555, 1000] }"


aws sns publish --topic-arn "arn:aws:sns:eu-west-1:603029492791:CAPAlertEvent" --message "file://./sns_test_1"
