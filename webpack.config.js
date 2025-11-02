const path = require('path');

module.exports = {
    entry: './src/main/webapp/templates/index.js',
    module: {
        rules: [
            {
                test: /\.hbs$/,
                loader: 'handlebars-loader',
                options: {
                    partialDirs: [
                        path.resolve(__dirname, 'src/main/webapp/templates'),
                    ],
                    helperDirs: [
                        path.resolve(__dirname, 'src/main/webapp/templates/helpers')
                    ]
                },
            }
        ]
    },
    output: {
        filename: 'templates-bundle.js',
        path: path.resolve(__dirname, 'src/main/webapp/js'),
    },
};
