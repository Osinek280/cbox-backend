#!/bin/bash

BUCKET="cbox-bucket"
LOCAL_FOLDER="/Komponenty"

echo "Sprawdzam, czy bucket $BUCKET istnieje..."
if awslocal s3 ls s3://$BUCKET >/dev/null 2>&1; then
    echo "Bucket $BUCKET już istnieje!"
else
    echo "Bucket $BUCKET nie istnieje, tworzę..."
    awslocal s3 mb s3://$BUCKET
fi

echo "Synchronizuję lokalny folder $LOCAL_FOLDER z bucketem $BUCKET..."
awslocal s3 sync $LOCAL_FOLDER s3://$BUCKET/ --delete

echo "Sync zakończony!"

# Konfiguracja CORS przy użyciu awslocal
echo "Ustawiam CORS na bucket $BUCKET..."
awslocal s3api put-bucket-cors --bucket $BUCKET --cors-configuration '{
  "CORSRules": [
    {
      "AllowedHeaders": ["*"],
      "AllowedMethods": ["GET"],
      "AllowedOrigins": ["*"]
    }
  ]
}'

echo "CORS ustawione!"