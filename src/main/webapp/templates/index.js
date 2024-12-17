import Handlebars from 'handlebars';
import tableBody from './table-body.hbs';
import worstTestsTableBody from './worst-tests-table-body.hbs';

window.Handlebars = Handlebars;
window.testResultAnalyzerTemplates = {
    'table-body': tableBody,
    'worst-tests-table-body': worstTestsTableBody
};
