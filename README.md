Использование:

Директории:
`java -jar path/to/jar/yaml-preproc.jar path/to/source-dir path/to/preprocessed-dir`

Файлы:

`java -jar path/to/jar/yaml-preproc.jar path/to/source-file.yml path/to/preprocessed-file.yml`


Примеры:

`java -jar D:\Examples\yaml-preproc.jar D:\Examples\source-dir D:\Examples\preprocessed-dir`

`java -jar D:\Examples\yaml-preproc.jar D:\Examples\source-dir\U1.yml D:\Examples\preprocessed-dir\U1.yml`


Примечание:

Если в исходном файле в качестве первой строки присутствует директива: 

`#no-preproc`

то соответствующий файл не будет препроцессирован.
