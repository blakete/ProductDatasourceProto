import pandas as pd
import requests as rq
import time

cvs_barcodes = pd.read_csv("cvs_barcodes.csv", dtype ='str')
barcodes = cvs_barcodes["Barcode"].tolist()

for barcode in barcodes:
	con = rq.get("http://localhost:8080/api/item/"+str(barcode)).content
	print(f"\nbarcode: {barcode}")
	print(con)
	time.sleep(2)


	
