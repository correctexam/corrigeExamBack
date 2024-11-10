from es import add_data_from_pdf_path

pdf_file_path = 'corrigeExamBackDAN/src/main/resources/rag/English_Course.pdf'

add_data_from_pdf_path(pdf_path=pdf_file_path, exam_name="exam", course_name="course")

#See Kibana at port 5601 for confirmation
# Note : Command for compose detached from terminal is : docker compose -f app.yml up -d